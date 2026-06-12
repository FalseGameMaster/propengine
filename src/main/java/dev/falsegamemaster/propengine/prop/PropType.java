package dev.falsegamemaster.propengine.prop;

import java.util.HashMap;
import java.util.Map;

public record PropType(String literal, PropRotationPolicy rotationPolicy) {

    public static PropType STATIC = new PropType("static", PropRotationPolicy.EULER);
    public static PropType DOOR = new PropType("door", PropRotationPolicy.CARDINAL);
    public static PropType ELEVATOR = new PropType("elevator", PropRotationPolicy.CARDINAL);
    public static PropType VEHICLE = new PropType("vehicle", PropRotationPolicy.EULER);
    public static PropType NPC = new PropType("npc", PropRotationPolicy.EULER);
    public static PropType MISC = new PropType("misc", PropRotationPolicy.EULER);

    public static PropType register(Registrar registrar, PropType propType) {
        registrar.registerIfMissing(propType);
        return propType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PropType(String otherLiteral, PropRotationPolicy otherRotationPolicy) && literal.equals(otherLiteral) && rotationPolicy == otherRotationPolicy;
    }

    public static class Registrar extends dev.falsegamemaster.propengine.registration.Registrar<PropType> {
        private final Map<String, PropType> propTypes = new HashMap<>();

        public Registrar() {}

        public boolean registerIfMissing(PropType entry) {
            return super.registerIfMissing(entry.literal, entry);
        }

        @Override
        protected Map<String, PropType> getEntriesInternal() {
            return propTypes;
        }

        @Override
        public Map<String, PropType> getEntries() {
            return Map.copyOf(propTypes);
        }
    }

}
