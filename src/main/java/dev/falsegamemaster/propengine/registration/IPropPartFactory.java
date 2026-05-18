package dev.falsegamemaster.propengine.registration;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.part.PropPart;

public interface IPropPartFactory<P extends PropPart<? extends Prop, ?>> {
    P createPropPart(Prop prop, int sequentialID);
}
