package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;

public abstract class PropPart<P extends Prop, I>  {

    public final P prop;
    public final int sequentialID;
    protected I internal;

    public PropPart(Class<P> propClass, Prop prop, int sequentialID) {
        assert propClass.isAssignableFrom(prop.getClass());
        this.prop = propClass.cast(prop);
        this.sequentialID = sequentialID;
    }

    public abstract String getCategory();

    public abstract String getLiteral();

    public abstract Class<I> getInternalClass();

    protected abstract void setupInternal(I internal, PropSpawnRequest request);

    public void prepareInternal(I internal, PropSpawnRequest request) {}

    public abstract void spawn(PropSpawnRequest request);

    public I getInternal() {
        return internal;
    }

}
