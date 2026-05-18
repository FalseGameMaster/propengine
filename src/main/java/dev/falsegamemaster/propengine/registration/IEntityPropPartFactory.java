package dev.falsegamemaster.propengine.registration;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import org.bukkit.entity.Entity;

@Deprecated
public interface IEntityPropPartFactory<P extends EntityPropPart<? extends Prop, ? extends Entity>> {
    P createPropPart(Prop prop, int sequentialID);
}
