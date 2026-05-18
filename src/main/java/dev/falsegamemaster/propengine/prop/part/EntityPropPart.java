package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropPersistentData;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class EntityPropPart<P extends Prop, E extends Entity> extends PropPart<P, E> {

    public EntityPropPart(Class<P> propClass, Prop prop, int sequentialID) {
        super(propClass, prop, sequentialID);
    }

    @Override
    protected final void setupInternal(E internal, PropSpawnRequest request) {
        AdvancedLocation location = request.location();
        internal.setInvulnerable(true);
        internal.setGravity(false);
        internal.setRotation(0, 0);
        PropPersistentData.write(internal, prop, location, request.data());
        String owner = "propengine";
        String type = prop.getPropType().literal();
        String domain0 = prop.getLiteral();
        String domain1 = domain0 + "." + getCategory();
        String domain2 = domain1 + "." + getLiteral();
        String domain3 = domain2 + "." + sequentialID;
        internal.addScoreboardTag(owner);
        internal.addScoreboardTag(owner + "." + type);
        internal.addScoreboardTag(prop.uniqueName);
        internal.addScoreboardTag(domain0);
        internal.addScoreboardTag(domain1);
        internal.addScoreboardTag(domain2);
        internal.addScoreboardTag(domain3);
    }

    @Override
    public final void spawn(PropSpawnRequest request) {
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

}
