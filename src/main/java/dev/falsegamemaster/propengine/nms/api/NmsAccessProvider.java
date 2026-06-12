package dev.falsegamemaster.propengine.nms.api;

import dev.falsegamemaster.propengine.nms.v1_21_1.PropEngineNmsAccessor_v1_21_1;
import org.bukkit.Bukkit;

public final class NmsAccessProvider {

    private NmsAccessProvider() {}

    public static PropEngineNmsAccessor create() {
        String version = Bukkit.getBukkitVersion();
        if (version.contains("1.21.1")) {
            return new PropEngineNmsAccessor_v1_21_1(version);
        }
        return new UnsupportedNmsAccessor(version);
    }

}
