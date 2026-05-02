package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import dev.falsegamemaster.propengine.prop.statics.TestStaticProp;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TestStaticPropPart extends EntityPropPart<TestStaticProp, ItemDisplay> {

    public TestStaticPropPart(Prop prop, int sequentialID) {
        super(TestStaticProp.class, prop, sequentialID);
    }

    @Override
    public String getCategory() {
        return "base";
    }

    @Override
    public String getLiteral() {
        return "test_static_prop_part";
    }

    @Override
    public Class<ItemDisplay> getInternalClass() {
        return ItemDisplay.class;
    }

    @Override
    public void prepareInternal(ItemDisplay entity, PropSpawnRequest request) {
        entity.setItemStack(new ItemStack(Material.GOLD_NUGGET)); // temporary placeholder
        entity.setPersistent(false);
        entity.setTeleportDuration(1);
        entity.setInterpolationDuration(1);
        entity.setRotation(0, 0);
        AdvancedLocation location = request.location();
        Quaternionf rotation = new Quaternionf()
            .rotateY((float) Math.toRadians(-location.getYaw()))
            .rotateX((float) Math.toRadians(location.getPitch()))
            .rotateZ((float) Math.toRadians(location.getRoll()));
        Transformation transformation = new Transformation(
            new Vector3f(0.5f, 4.5f, 0.5f),   // translation
            rotation,                         // left rotation
            new Vector3f(16.0f, 16.0f, 16.0f),// scale
            new Quaternionf()                 // right rotation
        );
        entity.setTransformation(transformation);
    }

}
