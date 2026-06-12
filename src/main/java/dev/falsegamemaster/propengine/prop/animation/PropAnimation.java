package dev.falsegamemaster.propengine.prop.animation;

import dev.falsegamemaster.propengine.prop.Prop;

import java.util.ArrayList;
import java.util.List;

public abstract class PropAnimation<P extends Prop> {

    public final String name;
    public final P prop;
    public final boolean isLooping;
    private final List<IPropAnimationFrame<P>> frames = new ArrayList<>();

    public PropAnimation(String name, Class<P> propClass, Prop prop, boolean isLooping) {
        assert propClass.isAssignableFrom(prop.getClass());
        this.name = name;
        this.prop = propClass.cast(prop);
        this.isLooping = isLooping;
        createFrames(frames);
    }

    public abstract void createFrames(List<IPropAnimationFrame<P>> frames);

    public List<IPropAnimationFrame<P>> getFrames() {
        return new ArrayList<>(frames);
    }

}
