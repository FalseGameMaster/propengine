package dev.falsegamemaster.propengine.prop.animation;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.part.PropPart;
import dev.falsegamemaster.propengine.prop.statics.PlayerCorpseProp;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.Transform;
import org.bukkit.entity.ItemDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class TestPlayerCorpsePropAnimation extends PropAnimation<PlayerCorpseProp> {

    public TestPlayerCorpsePropAnimation(Prop prop, boolean isLooping) {
        super(PlayerCorpseProp.class, prop, isLooping);
    }

    @Override
    public void createFrames(List<IPropAnimationFrame<PlayerCorpseProp>> frames) {
//        for (int i = 0; i < 32; i ++) {
//            frames.add(createFrame(i));
//        }
    }

//    private IPropAnimationFrame<PlayerCorpseProp> createFrame(int frameIndex) {
//        return IPropAnimationFrame.create(prop -> {
//            PropPart<PlayerCorpseProp, ItemDisplay> body = prop.getPartOLD(0), head = prop.getPartOLD(1);
//            if (body == null || head == null) return;
//            AdvancedLocation location = prop.getLocation();
//            Quaternionf propRotation = new Quaternionf().rotationY((float)(1./16 * Math.PI * frameIndex));
//            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 0.125f, -0.5f)).preScaleRotation(propRotation).build();
//
//            float bobMaxHeight = 0.5f;
//            float bobHeight = (float)(bobMaxHeight * (-0.5 * Math.cos(0.125*Math.PI*frameIndex) + 0.5));
//
//            Transform bodyPartTransform = new Transform(new Vector3f(-0.5f, -0.5f + bobHeight, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
//            Transform headPartTransform = new Transform(new Vector3f(-0.5f, -0.375f + bobHeight, 0.0f), new Vector3f(0.0f, -0.5f, 0.0f), new Quaternionf().rotationX((float)(0.25*Math.PI)), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
//            body.getInternal().setInterpolationDelay(0);
//            body.getInternal().setInterpolationDuration(4);
//            body.getInternal().setTransformation(propTransform.compose(bodyPartTransform).bake());
//            head.getInternal().setInterpolationDelay(0);
//            head.getInternal().setInterpolationDuration(4);
//            head.getInternal().setTransformation(propTransform.compose(headPartTransform).bake());
//        }, 4);
//    }

}
