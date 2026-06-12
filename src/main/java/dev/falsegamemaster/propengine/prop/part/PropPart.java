package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;

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

    public final String getKey() {
        return getCategory() + "." + getLiteral();
    }

    public final String getQualifiedName() {
        return prop.getLiteral() + "." + getKey();
    }

    public abstract Class<I> getInternalClass();

    protected abstract void setupInternal(I internal, Prop.SpawnRequest request);

    public void prepareInternal(I internal, Prop.SpawnRequest request) {}

    public abstract void spawn(Prop.SpawnRequest request);

    public abstract void unload();

    public I getInternal() {
        return internal;
    }

}
