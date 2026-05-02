package dev.falsegamemaster.propengine.registration;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import org.bukkit.entity.Entity;

public interface IPropPartFactory<P extends EntityPropPart<? extends Prop, ? extends Entity>> {
    P createPropPart(Prop prop, int sequentialID);
}
