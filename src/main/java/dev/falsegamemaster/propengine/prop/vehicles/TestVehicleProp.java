package dev.falsegamemaster.propengine.prop.vehicles;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;

import java.util.List;

@Deprecated
public class TestVehicleProp extends Prop {

    public TestVehicleProp(Prop.Registrar registrar, String uniqueFriendlyName) {
        super(registrar, uniqueFriendlyName);
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return TestVehicleProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.VEHICLE;
    }

    @Override
    public String getLiteral() {
        return "test_vehicle";
    }

    @Override
    public String getDisplayName() {
        return "Test Vehicle"; // TODO: Load from resource pack (4/11/2026)
    }

    // TODO: populate this (4/16/2026)
    @Override
    public List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request) {
        return null;
    }

}
