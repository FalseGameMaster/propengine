package dev.falsegamemaster.propengine.prop.animation;

import dev.falsegamemaster.propengine.prop.Prop;

import java.util.List;

public class PropAnimationPlayer<P extends Prop> {

    private final PropAnimation<P> animation;
    private int frameIndex = 0;
    private int ticksRemaining = 0;

    public PropAnimationPlayer(PropAnimation<P> animation) {
        this.animation = animation;
    }

    public boolean tick() {
        if (ticksRemaining > 0) {
            ticksRemaining --;
            return true;
        }
        List<IPropAnimationFrame<P>> frames = animation.getFrames();
        IPropAnimationFrame<P> frame = frames.get(frameIndex);
        frame.applyAction(animation.prop);
        ticksRemaining = Math.max(1, frame.durationTicks()) - 1;
        frameIndex ++;
        if (frameIndex >= frames.size()) {
            frameIndex = animation.isLooping ? 0 : frames.size() - 1;
            return animation.isLooping;
        }
        return false;
    }

}
