package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class EntityPropPart<P extends Prop, E extends Entity> extends PropPart<P, E> {

    public EntityPropPart(Class<P> propClass, Prop prop, int sequentialID) {
        super(propClass, prop, sequentialID);
    }

    private NamespacedKey pdcKey(String name) {
        return new NamespacedKey(PropEnginePlugin.getPlugin(PropEnginePlugin.class), name);
    }

    @Override
    protected final void setupInternal(E internal, Prop.SpawnRequest request) {
        internal.setInvulnerable(true);
        internal.setGravity(false);
        internal.setRotation(0, 0);
        String domain0 = prop.getLiteral();
        String domain1 = domain0 + "." + getCategory();
        String domain2 = domain1 + "." + getLiteral();
        String domain3 = domain2 + "." + sequentialID;
        for (String tag : prop.getTags()) {
            internal.addScoreboardTag(tag);
        }
        internal.addScoreboardTag(domain1);
        internal.addScoreboardTag(domain2);
        internal.addScoreboardTag(domain3);
        PersistentDataContainer pdc = internal.getPersistentDataContainer();
        pdc.set(pdcKey("pe.is_prop"), PersistentDataType.BOOLEAN, true);
        pdc.set(pdcKey("pe.literal"), PersistentDataType.STRING, prop.getLiteral());
        pdc.set(pdcKey("pe.unique_name"), PersistentDataType.STRING, prop.uniqueName);
    }

    @Override
    public final void spawn(Prop.SpawnRequest request) {
        AdvancedLocation location = request.location();
        if (location.getWorld() == null) return;
        Location spawnLoc = location.clone();
        spawnLoc.setYaw(0.0f);
        spawnLoc.setPitch(0.0f);
        this.internal = location.getWorld().spawn(spawnLoc, getInternalClass(), e -> {
            setupInternal(e, request);
            prepareInternal(e, request);
        });
    }

    @Override
    public void unload() {
        internal.remove();
        internal = null;
    }

}
