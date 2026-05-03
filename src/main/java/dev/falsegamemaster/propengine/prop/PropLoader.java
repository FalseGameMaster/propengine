package dev.falsegamemaster.propengine.prop;

import dev.falsegamemaster.propengine.PropEnginePlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropLoader {

    private PropLoader() {}

    public static void loadProps(PropEnginePlugin propEngine) {
        Map<String, PropSpawnRequest> requests = new LinkedHashMap<>();
        List<Entity> oldPropEntities = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                PropSpawnRequest request = PropPersistentData.read(entity);
                if (request == null) continue;
                oldPropEntities.add(entity);
                String key = request.literal() + ":" + request.uniqueFriendlyName();
                requests.putIfAbsent(key, request);
            }
        }
        for (Entity entity : oldPropEntities) {
            entity.remove();
        }
        for (PropSpawnRequest request : requests.values()) {
            Prop.spawn(propEngine, request);
        }
        PropEnginePlugin.LOGGER.info("Loaded " + requests.size() + " props.");
    }

}
