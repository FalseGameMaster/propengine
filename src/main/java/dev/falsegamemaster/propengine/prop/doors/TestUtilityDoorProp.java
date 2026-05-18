package dev.falsegamemaster.propengine.prop.doors;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.animation.IPropAnimationFrame;
import dev.falsegamemaster.propengine.prop.animation.PropAnimation;
import dev.falsegamemaster.propengine.prop.part.EntityPropPart;
import dev.falsegamemaster.propengine.prop.part.PropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.Transform;
import dev.falsegamemaster.propengine.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

@Deprecated
public class TestUtilityDoorProp extends Prop {

    public TestUtilityDoorProp(Prop.Registrar registrar, String uniqueFriendlyName) {
        super(registrar, uniqueFriendlyName);
        playAnimation(new OpenCloseAnimation(this, true));
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return TestUtilityDoorProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.DOOR;
    }

    @Override
    public String getLiteral() {
        return "test_utility_door";
    }

    @Override
    public String getDisplayName() {
        return "Test Utility Door";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request) {
        return List.of(FramePart::new, DoorPart::new);
    }

    public static class FramePart extends EntityPropPart<TestUtilityDoorProp, ItemDisplay> {
        public FramePart(Prop prop, int sequentialID) {
            super(TestUtilityDoorProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "base";
        }

        @Override
        public String getLiteral() {
            return "frame";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(Util.getItemWithCustomModelData(Material.PINK_SHULKER_BOX, 7));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class DoorPart extends EntityPropPart<TestUtilityDoorProp, ItemDisplay> {
        public DoorPart(Prop prop, int sequentialID) {
            super(TestUtilityDoorProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "door";
        }

        @Override
        public String getLiteral() {
            return "main";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(Util.getItemWithCustomModelData(Material.PINK_SHULKER_BOX, 77));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class OpenCloseAnimation extends PropAnimation<TestUtilityDoorProp> {
        public OpenCloseAnimation(Prop prop, boolean isLooping) {
            super(TestUtilityDoorProp.class, prop, isLooping);
        }

        @Override
        public void createFrames(List<IPropAnimationFrame<TestUtilityDoorProp>> frames) {
//            for (int i = 0; i < 2; i ++) {
//                frames.add(createFrame(i));
//            }
        }

//        private IPropAnimationFrame<TestUtilityDoorProp> createFrame(int frameIndex) {
//            return IPropAnimationFrame.create(prop -> {
//                PropPart<TestBlastDoorProp, ItemDisplay> doorPart = prop.getPartOLD(1);
//                if (doorPart == null) return;
//                AdvancedLocation location = prop.getLocation();
//                float yOffset = frameIndex == 0 ? 0 : 2.75f;
//                Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
//                Transform doorPartTransform = new Transform.Builder().translation(new Vector3f(-0.5f, 0.5f + yOffset, -0.5f)).build();
//                doorPart.getInternal().setInterpolationDelay(0);
//                doorPart.getInternal().setInterpolationDuration(20);
//                doorPart.getInternal().setTransformation(propTransform.compose(doorPartTransform).bake());
//            }, 40);
//        }
    }

}
