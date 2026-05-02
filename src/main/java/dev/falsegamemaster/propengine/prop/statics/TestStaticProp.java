package dev.falsegamemaster.propengine.prop.statics;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.part.TestStaticPropPart;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.registration.IPropPartFactory;

import java.util.List;

public class TestStaticProp extends Prop {

    public TestStaticProp(Prop.Registrar registrar, String uniqueFriendlyName) {
        super(registrar, uniqueFriendlyName);
    }

    @Override
    public IPropFactory<? extends Prop> getPropFactory() {
        return TestStaticProp::new;
    }

    @Override
    public PropType getPropType() {
        return PropType.STATIC;
    }

    @Override
    public String getLiteral() {
        return "test_prop";
    }

    @Override
    public String getDisplayName() {
        return "Test Prop";
    }

    @Override
    public List<IPropPartFactory<?>> getPartFactories(PropSpawnRequest request) {
        return List.of(TestStaticPropPart::new);
    }

}
