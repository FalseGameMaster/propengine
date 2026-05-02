package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;
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
        internal.setRotation(location.getYaw(), location.getPitch());
        String domain0 = "propengine";
        String domain1 = domain0 + "." + prop.getPropType().literal();
        String domain2 = domain1 + "." + prop.getLiteral();
        String domain3 = domain2 + "." + prop.uniqueFriendlyName;
        String domain4 = domain3 + "." + getCategory();
        String domain5 = domain4 + "." + getLiteral();
        String domain6 = domain5 + "." + sequentialID;
        internal.addScoreboardTag(domain0);
        internal.addScoreboardTag(domain1);
        internal.addScoreboardTag(domain2);
        internal.addScoreboardTag(domain3);
        internal.addScoreboardTag(domain4);
        internal.addScoreboardTag(domain5);
        internal.addScoreboardTag(domain6);

        // propengine.vehicle.sw_tie_fighter.<UUID>.seat.pilot.1

        // propengine.star_wars.tie_fighter.seat.<UUID HERE>

        // propengine.<namespace>.<prop_type>.<prop_uuid>.<part_type>.<part_name>
        // propengine.<namespace>.<prop_type>.<prop_name>.<part_type>.<part_name>

        // propengine.star_destroyer.tie_fighter.<UUID HERE>.seat.seat1
        // propengine.star_destroyer.tie_fighter.<UUID HERE>.seat.seat2
        // propengine.star_destroyer.tie_fighter.<UUID HERE>.seat.seat3

        // propengine.star_destroyer.tie_fighter.bob.seat.pilot
        // propengine.star_destroyer.tie_fighter.bob.body.main

        // propengine.star_destroyer.snow_speeder.kyle.body.1 -> Item Display Entity
        // propengine.star_destroyer.snow_speeder.kyle.pilot_seat.1 -> Armor Stand
        // propengine.star_destroyer.snow_speeder.kyle.gunner_seat.1 -> Armor Stand

        // propengine.star_destroyer.snow_speeder.kyle.body.1 -> Item Display Entity
        // propengine.star_destroyer.snow_speeder.kyle.seat.pilot.1 -> Armor Stand
        // propengine.star_destroyer.snow_speeder.kyle.seat.gunner.1 -> Armor Stand

        // propengine.star_destroyer.snow_speeder.kyle.body.main.1 -> Item Display Entity
        // propengine.star_destroyer.snow_speeder.kyle.seat.pilot.1 -> Armor Stand
        // propengine.star_destroyer.snow_speeder.kyle.seat.gunner.1 -> Armor Stand

        // propengine.star_wars.star_destroyer.snow_speeder.kyle.body.main.1 -> Item Display Entity
        // propengine.star_wars.star_destroyer.snow_speeder.kyle.seat.pilot.1 -> Armor Stand
        // propengine.star_wars.star_destroyer.snow_speeder.kyle.seat.gunner.1 -> Armor Stand

        // propengine.sw_snow_speeder.kyle.body.main.1 -> Item Display Entity
        // propengine.sw_snow_speeder.kyle.seat.pilot.1 -> Armor Stand
        // propengine.sw_snow_speeder.kyle.seat.gunner.1 -> Armor Stand

        // propengine.<prop_type>.<prop_name|prop_uuid>.<part_type>.<part_name>.<id_number>

        // propengine.<prop_type>.<prop_name>.<part_type>.<part_name>.<id_number>
    }

    @Override
    public void spawn(PropSpawnRequest request) {
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
