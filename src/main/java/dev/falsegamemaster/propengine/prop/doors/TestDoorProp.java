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

@Deprecated
public class TestDoorProp extends Prop {

    public TestDoorProp(Prop.Registrar registrar, String friendlyName) {
        super(registrar, friendlyName);
        playAnimation(new TestDoorOpenCloseAnimation(this, true));
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return TestDoorProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.DOOR;
    }

    @Override
    public String getLiteral() {
        return "test_door";
    }

    @Override
    public String getDisplayName() {
        return "Test Door";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request) {
        return List.of(FramePart::new, DoorPart::new);
    }

    public static class FramePart extends EntityPropPart<TestDoorProp, ItemDisplay> {
        public FramePart(Prop prop, int sequentialID) {
            super(TestDoorProp.class, prop, sequentialID);
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
            entity.setItemStack(new ItemStack(Material.GLASS));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(3.0f, 3.0f, 1.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class DoorPart extends EntityPropPart<TestDoorProp, ItemDisplay> {
        public DoorPart(Prop prop, int sequentialID) {
            super(TestDoorProp.class, prop, sequentialID);
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
            entity.setItemStack(new ItemStack(Material.CYAN_TERRACOTTA));
            entity.setRotation(0, 0);
            AdvancedLocation location = request.location();
            Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
            Transform partTransform = new Transform(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(3.0f, 3.0f, 1.0f), new Quaternionf());
            entity.setTransformation(propTransform.compose(partTransform).bake());
        }
    }

    public static class TestDoorOpenCloseAnimation extends PropAnimation<TestDoorProp> {
        public TestDoorOpenCloseAnimation(Prop prop, boolean isLooping) {
            super(TestDoorProp.class, prop, isLooping);
        }

        @Override
        public void createFrames(List<IPropAnimationFrame<TestDoorProp>> frames) {
            for (int i = 0; i < 2; i ++) {
                frames.add(createFrame(i));
            }
        }

        private IPropAnimationFrame<TestDoorProp> createFrame(int frameIndex) {
            return IPropAnimationFrame.create(prop -> {
                PropPart<TestDoorProp, ItemDisplay> doorPart = prop.getPart(1);
                if (doorPart == null) return;
                AdvancedLocation location = prop.getLocation();
                float yOffset = frameIndex == 0 ? 0 : 3;
                Transform propTransform = new Transform.Builder(location).pivot(new Vector3f(-0.5f, 1.0f, -0.5f)).build();
                Transform doorPartTransform = new Transform.Builder().translation(new Vector3f(-0.5f, 0.5f + yOffset, -0.5f)).scale(new Vector3f(3, 3, 1)).build();
                doorPart.getInternal().setInterpolationDelay(0);
                doorPart.getInternal().setInterpolationDuration(20);
                doorPart.getInternal().setTransformation(propTransform.compose(doorPartTransform).bake());
            }, 40);
        }
    }

}
