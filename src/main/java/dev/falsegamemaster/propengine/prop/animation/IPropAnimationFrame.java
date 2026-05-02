package dev.falsegamemaster.propengine.prop.animation;

import dev.falsegamemaster.propengine.prop.Prop;

import java.util.function.Consumer;

public interface IPropAnimationFrame<P extends Prop> {

    static <P extends Prop> IPropAnimationFrame<P> create(Consumer<P> action, int durationTicks) {
        return new IPropAnimationFrame<>() {
            @Override
            public void applyAction(P prop) {
                action.accept(prop);
            }

            @Override
            public int durationTicks() {
                return durationTicks;
            }
        };
    }

    void applyAction(P prop);

    int durationTicks();

}
