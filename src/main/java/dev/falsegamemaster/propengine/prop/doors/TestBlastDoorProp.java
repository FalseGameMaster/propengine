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
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class TestBlastDoorProp extends Prop {

    public TestBlastDoorProp(Prop.Registrar registrar, String friendlyName) {
        super(registrar, friendlyName);
        playAnimation(new TestDoorOpenCloseAnimation(this, true));
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return TestBlastDoorProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.DOOR;
    }

    @Override
    public String getLiteral() {
        return "test_blast_door";
    }

    @Override
    public String getDisplayName() {
        return "Test Blast Door";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request) {
        return List.of(FramePart::new, LeftDoorPart::new, RightDoorPart::new);
    }

    public static class FramePart extends EntityPropPart<TestBlastDoorProp, ItemDisplay> {
        public FramePart(Prop prop, int sequentialID) {
            super(TestBlastDoorProp.class, prop, sequentialID);
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

            entity.setItemStack(new ItemStack(Material.WET_SPONGE));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 1.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(5.0f, 5.0f, 5.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());

//            entity.setItemStack(new ItemStack(Material.GLASS));
//            entity.setRotation(0, 0);
//            AdvancedLocation location = request.location();
//            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
//            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(5.0f, 3.0f, 1.0f), new Quaternionf());
//            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class LeftDoorPart extends EntityPropPart<TestBlastDoorProp, ItemDisplay> {
        public LeftDoorPart(Prop prop, int sequentialID) {
            super(TestBlastDoorProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "door";
        }

        @Override
        public String getLiteral() {
            return "left";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(new ItemStack(Material.CYAN_TERRACOTTA));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(5.0f, 3.0f, 0.875f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class RightDoorPart extends EntityPropPart<TestBlastDoorProp, ItemDisplay> {
        public RightDoorPart(Prop prop, int sequentialID) {
            super(TestBlastDoorProp.class, prop, sequentialID);
        }

        @Override
        public String getCategory() {
            return "door";
        }

        @Override
        public String getLiteral() {
            return "right";
        }

        @Override
        public Class<ItemDisplay> getInternalClass() {
            return ItemDisplay.class;
        }

        @Override
        public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
            entity.setItemStack(new ItemStack(Material.CYAN_TERRACOTTA));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(5.0f, 3.0f, 0.875f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class TestDoorOpenCloseAnimation extends PropAnimation<TestBlastDoorProp> {
        public TestDoorOpenCloseAnimation(Prop prop, boolean isLooping) {
            super(TestBlastDoorProp.class, prop, isLooping);
        }

        @Override
        public void createFrames(List<IPropAnimationFrame<TestBlastDoorProp>> frames) {
            for (int i = 0; i < 2; i ++) {
                frames.add(createFrame(i));
            }
        }

        private IPropAnimationFrame<TestBlastDoorProp> createFrame(int frameIndex) {
            return IPropAnimationFrame.create(prop -> {
                PropPart<TestBlastDoorProp, ItemDisplay> leftDoorPart = prop.getPart(1), rightDoorPart = prop.getPart(2);
                if (leftDoorPart == null || rightDoorPart == null) return;
                AdvancedLocation location = prop.getLocation();
                float xOffset = frameIndex == 0 ? 0 : 5;
                Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
                Transform leftDoorPartTransform = new Transform.Builder().translation(new Vector3f(-0.5f + xOffset, 0.5f, -0.5f)).scale(new Vector3f(5.0f, 3.0f, 0.875f)).build();
                Transform rightDoorPartTransform = new Transform.Builder().translation(new Vector3f(-0.5f - xOffset, 0.5f, -0.5f)).scale(new Vector3f(5.0f, 3.0f, 0.875f)).build();
                leftDoorPart.getInternal().setInterpolationDelay(0);
                leftDoorPart.getInternal().setInterpolationDuration(20);
                leftDoorPart.getInternal().setTransformation(propTransform.compose(leftDoorPartTransform).bake());
                rightDoorPart.getInternal().setInterpolationDelay(0);
                rightDoorPart.getInternal().setInterpolationDuration(20);
                rightDoorPart.getInternal().setTransformation(propTransform.compose(rightDoorPartTransform).bake());
            }, 40);
        }
    }

}
