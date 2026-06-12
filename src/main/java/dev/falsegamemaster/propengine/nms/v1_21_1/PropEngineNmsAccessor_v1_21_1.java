package dev.falsegamemaster.propengine.nms.v1_21_1;

import dev.falsegamemaster.propengine.nms.api.CommandCaptureResult;
import dev.falsegamemaster.propengine.nms.api.PropEngineNmsAccessor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PropEngineNmsAccessor_v1_21_1 implements PropEngineNmsAccessor {

    // TODO (5/28/2026): move to internal static subclass for better organization
    public static String nmsComponentToPlainText(net.minecraft.network.chat.Component nmsComponent) {
        String json = CraftChatMessage.toJSON(nmsComponent);
        net.kyori.adventure.text.Component kyoriComponent = GsonComponentSerializer.gson().deserialize(json);
        return PlainTextComponentSerializer.plainText().serialize(kyoriComponent);
    }

    private final String version;

    public PropEngineNmsAccessor_v1_21_1(String version) {
        this.version = version;
    }

    @Override
    public String minecraftVersion() {
        return version;
    }

    // TODO (5/28/2026): move to internal static subclass for better organization
    @Override
    public CommandCaptureResult dispatchCommandCapturingOutput(CommandSender sender, String command) {
        if (!Bukkit.isPrimaryThread()) {
            return new CommandCaptureResult(false, 0, List.of("Command capture must be run on the main server thread."));
        }
        List<String> messages = new ArrayList<>();
        CommandSource capturingSource = new CommandSource() {
            @Override
            public void sendSystemMessage(@NotNull Component component) { messages.add(nmsComponentToPlainText(component)); }
            @Override
            public boolean acceptsSuccess() { return true; }
            @Override
            public boolean acceptsFailure() { return true; }
            @Override
            public boolean shouldInformAdmins() { return false; }
            @Override
            public @NotNull CommandSender getBukkitSender(@NotNull CommandSourceStack commandSourceStack) { return sender; }
        };
        try {
            CraftServer craftServer = (CraftServer) Bukkit.getServer();
            MinecraftServer minecraftServer = craftServer.getServer();
            CommandSourceStack baseStack;
            if (sender instanceof Player player) {
                baseStack = ((CraftPlayer) player).getHandle().createCommandSourceStack();
            } else {
                baseStack = minecraftServer.createCommandSourceStack();
            }
            AtomicBoolean success = new AtomicBoolean(false);
            AtomicInteger result = new AtomicInteger(0);
            CommandSourceStack captureStack = baseStack.withSource(capturingSource).withPermission(4).withCallback((_success, _result) -> {
                success.set(_success);
                result.set(_result);
            });
            minecraftServer.getCommands().performPrefixedCommand(captureStack, command);
            return new CommandCaptureResult(success.get(), result.get(), messages);
        } catch (Throwable throwable) {
            messages.add("NMS command capture failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            return new CommandCaptureResult(false, 0, messages);
        }
    }

}
