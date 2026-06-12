package dev.falsegamemaster.propengine.nms.api;

import org.bukkit.command.CommandSender;

public final class UnsupportedNmsAccessor implements PropEngineNmsAccessor {

    private final String version;

    public UnsupportedNmsAccessor(String version) {
        this.version = version;
    }

    @Override
    public String minecraftVersion() {
        return version;
    }

    @Override
    public CommandCaptureResult dispatchCommandCapturingOutput(CommandSender sender, String command) {
        throw new UnsupportedOperationException("PropEngine NMS is not implemented for Minecraft/Paper version " + version);
    }

}
