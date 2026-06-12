package dev.falsegamemaster.propengine.prop;

import dev.falsegamemaster.propengine.prop.ai.PropAI;

import java.util.HashMap;
import java.util.Map;

public class PropControllerRegistrar {

    private final Map<String, PropAI.Controller> propControllers = new HashMap<>();

    public void add(PropAI.Controller controller) {
        propControllers.put(controller.getProp().uniqueName, controller);
    }

    public void remove(PropAI.Controller controller) {
        propControllers.remove(controller.getProp().uniqueName);
    }

    public void tick() {
        for (PropAI.Controller controller : propControllers.values()) {
            controller.tick();
        }
    }

}
