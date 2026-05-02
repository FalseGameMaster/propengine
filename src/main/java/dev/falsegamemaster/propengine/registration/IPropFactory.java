package dev.falsegamemaster.propengine.registration;

import dev.falsegamemaster.propengine.prop.Prop;

public interface IPropFactory<P extends Prop> {
    P createProp(Prop.Registrar registrar, String uniqueFriendlyName);
}
