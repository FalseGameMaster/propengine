package dev.falsegamemaster.propengine.prop;

import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.PropDataParser;
import dev.falsegamemaster.propengine.util.Util;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;

public class PropCodec {

    private static void run(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String encodeLocation(AdvancedLocation location) {
        return "{"
            + "world:" + quote(location.getWorld().getName()) + ","
            + "x:" + location.x() + "d,"
            + "y:" + location.y() + "d,"
            + "z:" + location.z() + "d,"
            + "yaw:" + location.getYaw() + "f,"
            + "pitch:" + location.getPitch() + "f,"
            + "roll:" + location.getRoll() + "f"
            + "}";
    }

    private static AdvancedLocation decodeLocation(CompoundTag locationTag) {
        World world = Bukkit.getWorld(locationTag.getStringTag("world").getValue());
        double x = locationTag.getDoubleTag("x").asDouble();
        double y = locationTag.getDoubleTag("y").asDouble();
        double z = locationTag.getDoubleTag("z").asDouble();
        float yaw = locationTag.getFloatTag("yaw").asFloat();
        float pitch = locationTag.getFloatTag("pitch").asFloat();
        float roll = locationTag.getFloatTag("roll").asFloat();
        return new AdvancedLocation(world, x, y, z, yaw, pitch, roll);
    }

    public final String databaseName;

    public PropCodec(String databaseName) {
        this.databaseName = databaseName;
    }

    public void encodeProp(Prop.SpawnRequest request) {
        String path = "props." + quote(request.key());
        run("data modify storage " + databaseName + " " + path + ".literal set value " + quote(request.literal()));
        run("data modify storage " + databaseName + " " + path + ".uniqueName set value " + quote(request.uniqueName()));
        run("data modify storage " + databaseName + " " + path + ".location set value " + encodeLocation(request.location()));
        if (request.data() != null) {
            run("data modify storage " + databaseName + " " + path + ".data set value " + quote(request.data().toString()));
        } else {
            run("data modify storage " + databaseName + " " + path + ".data set value " + quote("{}"));
        }
    }

    public void removeProp(String key) {
        run("data remove storage " + databaseName + " props." + quote(key));
    }

    public Prop.SpawnRequest decodeProp(CompoundTag propsTag) {
        if (!propsTag.containsKey("literal") || !propsTag.containsKey("uniqueName") || !propsTag.containsKey("location") || !propsTag.containsKey("data")) return null;
        String literal = propsTag.getStringTag("literal").getValue();
        String uniqueName = propsTag.getStringTag("uniqueName").getValue();
        AdvancedLocation location = decodeLocation(propsTag.getCompoundTag("location"));
        JsonObject data = PropDataParser.parseObject(propsTag.getStringTag("data").getValue());
        data = data != null && data.isEmpty() ? null : data;
        return new Prop.SpawnRequest(literal, uniqueName, location, data);
    }

    public Prop.SpawnRequest decodeProp(String key) {
        return decodeProp(Util.Data.getTagFromMinecraftStorage(databaseName, "props." + key));
    }

    // TODO (5/29/2026): create a better, abstracted way of cleaning up residual props, as all prop parts are not necessarily entities
    private void removeOldProps() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : Util.Entity.getEntitiesByTag(world, "propengine")) {
                entity.remove();
            }
        }
    }

    public void loadRegistryFromStorage() {
        CompoundTag propsTag = Util.Data.getTagFromMinecraftStorage(databaseName, "props");
        for (String propKey : propsTag.keySet()) {
            Prop.SpawnRequest request = decodeProp(propsTag.getCompoundTag(propKey));
            Prop.create(PropEnginePlugin.getInstance(), request, true);
        }
    }

    public void loadAllProps() {
        removeOldProps();
        for (Prop prop : Prop.getProps().values()) {
            prop.load();
        }
    }

    public void unloadAllProps() {
        for (Prop prop : Prop.getProps().values()) {
            prop.unload();
        }
    }

    public void destroyAllProps() {
        for (Prop prop : List.copyOf(Prop.getProps().values())) {
            prop.destroy();
        }
    }

}
