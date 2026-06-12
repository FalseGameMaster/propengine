package dev.falsegamemaster.propengine.prop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.ai.PropAI;
import dev.falsegamemaster.propengine.prop.animation.PropAnimation;
import dev.falsegamemaster.propengine.prop.animation.PropAnimationManager;
import dev.falsegamemaster.propengine.prop.animation.PropAnimationPlayer;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import dev.falsegamemaster.propengine.prop.part.PropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.GenericsUtil;
import dev.falsegamemaster.propengine.util.TagExpression;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

public abstract class Prop {

    private static final Map<String, Prop> PROPS = new HashMap<>();

    public static Prop dummy(IPropFactory<?> factory) {
        return factory.createProp(null, "_dummy");
    }

    public static Prop register(Registrar registrar, IPropFactory<?> factory) {
        return factory.createProp(registrar, "_template");
    }

    public static Map<String, Prop> getProps() {
        return PROPS;
    }

    public static List<Prop> getPropsMatchingTags(String expression) {
        TagExpression tagExpression = TagExpression.Parser.parse(expression);
        return PROPS.values().stream().filter(prop -> tagExpression.test(prop.getTags())).toList();
    }

    private static NamespacedKey pdcKey(String name) {
        return new NamespacedKey(PropEnginePlugin.getPlugin(PropEnginePlugin.class), name);
    }

    public static Prop getPropFromEntity(Entity entity) {
        if (entity == null) return null;
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(pdcKey("pe.is_prop"))) return null;
        String literal = pdc.get(pdcKey("pe.literal"), PersistentDataType.STRING);
        String uniqueName = pdc.get(pdcKey("pe.unique_name"), PersistentDataType.STRING);
        return getProps().get(literal + "." + uniqueName);
    }

    public static Prop create(PropEnginePlugin plugin, SpawnRequest request, boolean register) {
        IPropFactory<? extends Prop> factory = plugin.propRegistrar.getEntries().get(request.literal());
        if (factory == null) return null;
        Prop prop = factory.createProp(plugin.propRegistrar, request.uniqueName());
        prop.spawnRequest = request;
        prop.location = request.location();
        prop.data = request.data();
        prop.addTagsFromData();
        if (register) PROPS.put(request.key(), prop);
        return prop;
    }

    public static Prop spawnNew(PropEnginePlugin plugin, SpawnRequest request) {
        Prop prop = create(plugin, request, true);
        if (prop == null) return null;
        JsonObject data = request.data();
        if (data != null && data.has("isPersistent") && data.get("isPersistent").isJsonPrimitive()) {
            String booleanString = data.get("isPersistent").getAsString().toLowerCase();
            if (booleanString.equals("true") || booleanString.equals("false")) {
                boolean isPersistent = data.get("isPersistent").getAsBoolean();
                if (isPersistent) plugin.propCodec.encodeProp(request);
            }
        }
        prop.load();
        return prop;
    }

    protected final Registrar propTypeRegistrar;
    public final String uniqueName;
    protected final Map<String, PropPart<?, ?>> parts = new HashMap<>();
    protected final Set<String> tags = new HashSet<>();
    protected AdvancedLocation location = null;
    protected JsonObject data = null;
    protected LifecycleState lifecycleState = LifecycleState.UNLOADED;
    protected SpawnRequest spawnRequest = null;
    private final Map<String, PropAnimation<? extends Prop>> registeredAnimations = new HashMap<>();
//    @Deprecated protected final Map<String, PropAnimationPlayer<? extends Prop>> activeAnimationPlayers = new HashMap<>();
    protected PropAnimationPlayer<? extends Prop> activeAnimationPLayer = null;

    // TODO (5/30/2026): perhaps create a constructor with more parameters?
    protected Prop(Registrar propTypeRegistrar, String uniqueName) {
        if (propTypeRegistrar != null && propTypeRegistrar.registerIfMissing(getLiteral(), getPropFactory())) PropEnginePlugin.LOGGER.info("Registered new prop: " + getLiteral() + ", Type: " + getPropType().literal());
        this.propTypeRegistrar = propTypeRegistrar;
        this.uniqueName = uniqueName;
        addDefaultTags();
    }

    private void addDefaultTags() {
        addTag("propengine");
        addTag("propengine." + getPropType().literal());
        addTag(getLiteral());
        addTag(uniqueName);
    }

    private void addTagsFromData() {
        if (data == null || !data.has("tags") || !data.get("tags").isJsonArray()) return;
        JsonArray tagArray = data.get("tags").getAsJsonArray();
        for (JsonElement e : tagArray) {
            if (!e.isJsonPrimitive()) throw new IllegalArgumentException("Unexpected JSON element found in the 'tags' array. This array must only contain strings");
            addTag(e.getAsString());
        }
    }

    public abstract IPropFactory<? extends Prop> getPropFactory();

    public abstract PropType getPropType();

    public abstract String getLiteral();

    public String getKey() {
        return getLiteral() + "." + uniqueName;
    }

    public abstract String getDisplayName();

    public abstract List<IPropPartFactory<?>> getPartFactories(SpawnRequest request);

    public Map<String, PropPart<?, ?>> getParts() {
        return new HashMap<>(parts);
    }

    public <P extends Prop, I> PropPart<P, I> getPart(String name) {
        return !parts.isEmpty() ? GenericsUtil.castOrNull(parts.get(name)) : null;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public Set<String> getTags() {
        return Set.copyOf(tags);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public AdvancedLocation getLocation() {
        assert location != null : "Prop location is not initialized";
        return new AdvancedLocation(location.getWorld(), location.x(), location.y(), location.z(), location.getYaw(), location.getPitch(), location.getRoll());
    }

    @Nullable
    public JsonObject getData() {
        if (data == null) return null;
        return data.deepCopy();
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public void load() {
        if (lifecycleState == LifecycleState.LOADED) return;
        parts.clear();
        List<IPropPartFactory<?>> factories = getPartFactories(spawnRequest);
        for (int i = 0; i < factories.size(); i ++) {
            PropPart<?, ?> part = factories.get(i).createPropPart(this, i);
            parts.put(part.getKey(), part);
            part.spawn(spawnRequest);
        }
        lifecycleState = LifecycleState.LOADED;
    }

    public void unload() {
        if (lifecycleState != LifecycleState.LOADED) return;
        stopAnimation(); // TODO (5/30/2026): perhaps unneeded if the parts will be despawned anyways?
        for (PropPart<?, ?> part : parts.values()) {
            part.unload();
        }
        parts.clear();
        lifecycleState = LifecycleState.UNLOADED;
    }

    public void destroy() {
        unload();
        PropEnginePlugin.getInstance().propCodec.removeProp(spawnRequest.key());
        PROPS.remove(spawnRequest.key());
        lifecycleState = LifecycleState.DESTROYED;
    }

    public void teleportTo(Location destination) {
        Vector delta = destination.toVector().subtract(location.toVector());
        for (PropPart<?, ?> part : getParts().values()) {
            if (!(part instanceof EntityPropPart<?, ?> entityPart)) continue;
            Entity entity = entityPart.getInternal();
            Location next = entity.getLocation().clone().add(delta);
            next.setYaw(destination.getYaw());
            next.setPitch(destination.getPitch());
            entity.teleport(next);
        }
        this.location = new AdvancedLocation(destination);
    }

    public void moveBy(Vector delta, float yaw, float pitch) {
        for (PropPart<?, ?> part : getParts().values()) {
            if (!(part instanceof EntityPropPart<?, ?> entityPart)) continue;
            Entity entity = entityPart.getInternal();
            Location next = entity.getLocation().clone().add(delta);
            next.setYaw(yaw);
            next.setPitch(pitch);
            entity.teleport(next);
        }
        this.location.add(delta);
        this.location.setYaw(yaw);
        this.location.setPitch(pitch);
    }

    public final void registerAnimation(PropAnimation<?> animation) {
        registeredAnimations.put(animation.name, animation);
    }

    public final Map<String, PropAnimation<?>> getRegisteredAnimations() {
        return Map.copyOf(registeredAnimations);
    }

//    @Deprecated
//    public final Map<String, PropAnimationPlayer<?>> getActiveAnimationPlayers() {
//        return Map.copyOf(activeAnimationPlayers);
//    }

    public final void tickAnimations() {
        if (activeAnimationPLayer == null) return;
        boolean shouldContinue = activeAnimationPLayer.tick();
        if (!shouldContinue) propTypeRegistrar.animationManager.remove(this);
    }

//    @Deprecated
//    public final void tickAnimationsOLD() {
//        List<String> cancelledPlayers = new ArrayList<>();
//        for (String player : activeAnimationPlayers.keySet()) {
//            boolean shouldContinue = activeAnimationPlayers.get(player).tick();
//            if (!shouldContinue) cancelledPlayers.add(player);
//        }
//        for (String player : cancelledPlayers) {
//            activeAnimationPlayers.remove(player);
//        }
//        if (propTypeRegistrar != null && activeAnimationPlayers.isEmpty()) propTypeRegistrar.animationManager.remove(this);
//    }

    public final void playAnimation(String animationName) {
        PropAnimation<?> animation = registeredAnimations.get(animationName);
        assert animation != null && this.getClass().isAssignableFrom(animation.prop.getClass());
        activeAnimationPLayer = new PropAnimationPlayer<>(animation);
        propTypeRegistrar.animationManager.add(this);
    }

//    @Deprecated
//    public final void playAnimationOLD(String animationName) {
//        PropAnimation<?> animation = registeredAnimations.get(animationName);
//        assert animation != null && this.getClass().isAssignableFrom(animation.prop.getClass());
//        activeAnimationPlayers.put(animation.name, new PropAnimationPlayer<>(animation));
//        if (propTypeRegistrar != null && activeAnimationPlayers.size() == 1) propTypeRegistrar.animationManager.add(this);
//    }
//
//    @Deprecated
//    public final void stopAnimationOLD(String animationName) {
//        PropAnimation<?> animation = registeredAnimations.get(animationName);
//        assert animation != null && this.getClass().isAssignableFrom(animation.prop.getClass());
//        activeAnimationPlayers.remove(animation.name);
//        if (propTypeRegistrar != null && activeAnimationPlayers.isEmpty()) propTypeRegistrar.animationManager.remove(this);
//    }

    public final void stopAnimation() {
//        activeAnimationPlayers.clear();
        activeAnimationPLayer = null;
        propTypeRegistrar.animationManager.remove(this);
    }

    public final void setController(PropAI.Controller controller) {
        propTypeRegistrar.controllers.add(controller);
    }

    public enum LifecycleState {
        UNLOADED,
        LOADED,
        DESTROYED
    }

    public record SpawnRequest(String literal, String uniqueName, AdvancedLocation location, @Nullable JsonObject data) {
        public String key() {
            return literal + "." + uniqueName;
        }
    }

    public static class Registrar extends dev.falsegamemaster.propengine.registration.Registrar<IPropFactory<? extends Prop>> {
        private final Map<String, IPropFactory<? extends Prop>> props = new HashMap<>();
        public final PropAnimationManager animationManager = new PropAnimationManager();
        public final PropControllerRegistrar controllers = new PropControllerRegistrar();

        public Registrar() {}

        @Override
        protected Map<String, IPropFactory<? extends Prop>> getEntriesInternal() {
            return props;
        }

        @Override
        public Map<String, IPropFactory<? extends Prop>> getEntries() {
            return Map.copyOf(props);
        }
    }

}
