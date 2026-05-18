package dev.falsegamemaster.propengine.prop;

import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.PropDataParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PropPersistentData {

    private static NamespacedKey key(String name) {
        return new NamespacedKey(PropEnginePlugin.getPlugin(PropEnginePlugin.class), name);
    }

    public static final NamespacedKey IS_PROP = key("is_prop");
    public static final NamespacedKey LITERAL = key("literal");
    public static final NamespacedKey UNIQUE_NAME = key("unique_name");
    public static final NamespacedKey WORLD = key("world");
    public static final NamespacedKey X = key("x");
    public static final NamespacedKey Y = key("y");
    public static final NamespacedKey Z = key("z");
    public static final NamespacedKey YAW = key("yaw");
    public static final NamespacedKey PITCH = key("pitch");
    public static final NamespacedKey ROLL = key("roll");
    public static final NamespacedKey DATA = key("data");

    private PropPersistentData() {}

    public static void write(Entity entity, Prop prop, AdvancedLocation location, JsonObject data) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(IS_PROP, PersistentDataType.BOOLEAN, true);
        pdc.set(LITERAL, PersistentDataType.STRING, prop.getLiteral());
        pdc.set(UNIQUE_NAME, PersistentDataType.STRING, prop.uniqueName);
        pdc.set(WORLD, PersistentDataType.STRING, location.getWorld().getName());
        pdc.set(X, PersistentDataType.DOUBLE, location.getX());
        pdc.set(Y, PersistentDataType.DOUBLE, location.getY());
        pdc.set(Z, PersistentDataType.DOUBLE, location.getZ());
        pdc.set(YAW, PersistentDataType.FLOAT, location.getYaw());
        pdc.set(PITCH, PersistentDataType.FLOAT, location.getPitch());
        pdc.set(ROLL, PersistentDataType.FLOAT, location.getRoll());
        if (data != null) pdc.set(DATA, PersistentDataType.STRING, data.toString());
    }

    public static PropSpawnRequest read(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!Boolean.TRUE.equals(pdc.get(IS_PROP, PersistentDataType.BOOLEAN))) return null;
        String literal = pdc.get(LITERAL, PersistentDataType.STRING);
        String uniqueName = pdc.get(UNIQUE_NAME, PersistentDataType.STRING);
        String worldName = pdc.get(WORLD, PersistentDataType.STRING);
        Double x = pdc.get(X, PersistentDataType.DOUBLE);
        Double y = pdc.get(Y, PersistentDataType.DOUBLE);
        Double z = pdc.get(Z, PersistentDataType.DOUBLE);
        Float yaw = pdc.get(YAW, PersistentDataType.FLOAT);
        Float pitch = pdc.get(PITCH, PersistentDataType.FLOAT);
        Float roll = pdc.get(ROLL, PersistentDataType.FLOAT);
        if (literal == null || uniqueName == null || worldName == null || x == null || y == null || z == null || yaw == null || pitch == null || roll == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        AdvancedLocation location = new AdvancedLocation(world, x, y, z, yaw, pitch, roll);
        JsonObject data = null;
        String rawData = pdc.get(DATA, PersistentDataType.STRING);
        if (rawData != null) data = PropDataParser.parseObject(rawData);
        return new PropSpawnRequest(literal, uniqueName, location, data);
    }
}
