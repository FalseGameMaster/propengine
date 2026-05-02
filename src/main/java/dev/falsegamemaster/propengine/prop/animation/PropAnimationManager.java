package dev.falsegamemaster.propengine.prop.animation;

import dev.falsegamemaster.propengine.prop.Prop;

import java.util.HashSet;
import java.util.Set;

public class PropAnimationManager {

    private final Set<Prop> animatedProps = new HashSet<>();

    public void add(Prop prop) {
        animatedProps.add(prop);
    }

    public void remove(Prop prop) {
        animatedProps.remove(prop);
    }

    public void tick() {
        for (Prop prop : animatedProps) {
            prop.tickAnimations();
        }
    }

}
