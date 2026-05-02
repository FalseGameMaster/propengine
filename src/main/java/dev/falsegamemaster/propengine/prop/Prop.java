package dev.falsegamemaster.propengine.prop;

import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.animation.PropAnimation;
import dev.falsegamemaster.propengine.prop.animation.PropAnimationManager;
import dev.falsegamemaster.propengine.prop.animation.PropAnimationPlayer;
import dev.falsegamemaster.propengine.prop.part.PropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.GenericsUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Prop {

    public static Prop dummy(IPropFactory<?> factory) {
        return factory.createProp(null, "_dummy");
    }

    public static Prop register(Registrar registrar, IPropFactory<?> factory) {
        return factory.createProp(registrar, "_template");
    }

    public static void spawn(PropEnginePlugin propEngine, PropSpawnRequest request) {
        IPropFactory<? extends Prop> factory = propEngine.PROP_REGISTRAR.getEntries().get(request.literal());
        if (factory == null) return;
        factory.createProp(propEngine.PROP_REGISTRAR, request.uniqueFriendlyName()).spawn(request);
    }

    protected final Registrar registrar;
    public final String uniqueFriendlyName;
    protected final List<PropPart<?, ?>> parts = new ArrayList<>();
    protected AdvancedLocation location = null;
    protected JsonObject data = null;
    protected final List<PropAnimationPlayer<? extends Prop>> animationPlayers = new ArrayList<>();

    public Prop(Registrar registrar, String uniqueFriendlyName) {
        if (registrar != null && registrar.registerIfMissing(getLiteral(), getPropFactory())) PropEnginePlugin.LOGGER.info("Registered new prop: " + getLiteral() + ", Type: " + getPropType().literal());
        this.registrar = registrar;
        this.uniqueFriendlyName = uniqueFriendlyName;
    }

    public abstract IPropFactory<? extends Prop> getPropFactory();

    public abstract PropType getPropType();

    public abstract String getLiteral();

    public abstract String getDisplayName();

    public abstract List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request);

    @Nullable
    public <P extends Prop, I> PropPart<P, I> getPart(int index) {
        return !parts.isEmpty() ? GenericsUtil.castOrNull(parts.get(index)) : null;
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

    public void spawn(PropSpawnRequest request) {
        location = request.location();
        data = request.data();
        parts.clear();
        List<IPropPartFactory<?>> partFactories = getPartFactories(request);
        for (int i = 0; i < partFactories.size(); i ++) {
            PropPart<?, ?> part = partFactories.get(i).createPropPart(this, i);
            parts.add(part);
            part.spawn(request);
        }
    }

    // TODO: maybe deprecate this? (4/16/2026)
    public void despawn() {}

    public final void tickAnimations() {
        for (PropAnimationPlayer<? extends Prop> player : animationPlayers) {
            player.tick();
        }
    }

    public final void playAnimation(PropAnimation<? extends Prop> animation) {
        assert this.getClass().isAssignableFrom(animation.prop.getClass());
        animationPlayers.add(new PropAnimationPlayer<>(animation));
        if (registrar != null && animationPlayers.size() == 1) registrar.animationManager.add(this);
    }

    public final void stopAnimation(PropAnimation<? extends Prop> animation) {
        assert this.getClass().isAssignableFrom(animation.prop.getClass());
        animationPlayers.remove(new PropAnimationPlayer<>(animation));
        if (registrar != null && animationPlayers.isEmpty()) registrar.animationManager.remove(this);
    }

    public static class Registrar extends dev.falsegamemaster.propengine.registration.Registrar<IPropFactory<? extends Prop>> {
        private final Map<String, IPropFactory<? extends Prop>> props = new HashMap<>();
        public final PropAnimationManager animationManager = new PropAnimationManager();

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
