package dev.falsegamemaster.propengine.nms.api;

import org.bukkit.command.CommandSender;

public interface PropEngineNmsAccessor {

    String minecraftVersion();

    CommandCaptureResult dispatchCommandCapturingOutput(CommandSender sender, String command);

}
