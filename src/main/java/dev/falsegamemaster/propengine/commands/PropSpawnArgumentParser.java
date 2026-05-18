package dev.falsegamemaster.propengine.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.*;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import dev.falsegamemaster.propengine.util.AdvancedLocation;
import dev.falsegamemaster.propengine.util.PropDataParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class PropSpawnArgumentParser {

    private PropSpawnArgumentParser() {}

    public static @Nullable PropSpawnRequest parse(PropEnginePlugin propEngine, CommandContext<CommandSourceStack> context, PropType propType, PropRotationMode rotationMode, boolean explicitDimension) {
        CommandSender sender = context.getSource().getSender();
        Player player = sender instanceof Player p ? p : null;
        String literal = StringArgumentType.getString(context, "literal");
        IPropFactory<?> propFactory = propEngine.propRegistrar.getEntries().get(literal);
        if (propFactory == null) { sender.sendPlainMessage("Unknown prop: " + literal); return null; }
        if (!Objects.equals(Prop.dummy(propFactory).getPropType(), propType)) { sender.sendPlainMessage("Prop " + literal + " is not of type " + propType + "."); return null; }
        World world;
        if (explicitDimension) {
            String worldName = StringArgumentType.getString(context, "dimension");
            world = Bukkit.getWorld(worldName);
            if (world == null) { sender.sendPlainMessage("Unknown dimension/world: " + worldName); return null; }
        } else {
            if (player == null) { sender.sendPlainMessage("Console must specify a dimension using 'in <dimension>'."); return null; }
            world = player.getWorld();
        }
        Location baseLocation = player != null ? player.getLocation() : null;
        FinePositionResolver position = context.getArgument("position", FinePositionResolver.class);
        FinePosition resolved;
        try { resolved = position.resolve(context.getSource());
        } catch (CommandSyntaxException e) { PropEnginePlugin.LOGGER.severe("Invalid position syntax: " + e.getMessage()); return null; }
        double x = resolved.x(), y = resolved.y(), z = resolved.z();
        float yaw, pitch, roll;
        if (rotationMode == PropRotationMode.FACING) {
            String facing = StringArgumentType.getString(context, "facing").toLowerCase();
            switch (facing) {
                case "n", "north" -> yaw = 180.0f;
                case "e", "east" -> yaw = -90.0f;
                case "s", "south" -> yaw = 0.0f;
                case "w", "west" -> yaw = 90.0f;
                default -> { sender.sendPlainMessage("Facing must be one of: north, east, south, west, n, e, s, w"); return null; }
            }
            pitch = 0.0f; roll = 0.0f;
        } else {
            if (propType.rotationPolicy() != PropRotationPolicy.EULER) { sender.sendPlainMessage("This prop type does not support arbitrary rotation."); return null; }
            Float parsedYaw = parseRelativeFloat(StringArgumentType.getString(context, "yaw"), baseLocation != null, baseLocation != null ? baseLocation.getYaw() : 0.0f);
            Float parsedPitch = parseRelativeFloat(StringArgumentType.getString(context, "pitch"), baseLocation != null, baseLocation != null ? baseLocation.getPitch() : 0.0f);
            Float parsedRoll = parseRelativeFloat(StringArgumentType.getString(context, "roll"), false, 0.0f);
            if (parsedYaw == null || parsedPitch == null || parsedRoll == null) { sender.sendPlainMessage("Invalid yaw, pitch, or roll."); return null; }
            yaw = parsedYaw + 180; pitch = parsedPitch; roll = parsedRoll;
        }
        JsonObject data = null;
        try { data = PropDataParser.parseObject(StringArgumentType.getString(context, "data"));
        } catch (IllegalArgumentException e) {
            PropEnginePlugin.LOGGER.warning("Custom JSON data could not be parsed for prop: " + e.getMessage());
        }
        String uniqueName = Optional.ofNullable(parseUniqueFriendlyName(data)).orElse(UUID.randomUUID().toString());
        return new PropSpawnRequest(literal, uniqueName, new AdvancedLocation(world, x, y, z, yaw, pitch, roll), data);
    }

    @Nullable
    private static String parseUniqueFriendlyName(@Nullable JsonObject data) {
        if (data == null || !data.has("name")) return null;
        JsonElement element = data.get("name");
        if (!element.isJsonPrimitive()) return null;
        return element.getAsString();
    }

    private static @Nullable Float parseRelativeFloat(String input, boolean allowRelative, float base) {
        if ("~".equals(input)) return allowRelative ? base : null;
        if (input.startsWith("~")) {
            if (!allowRelative) return null;
            String remainder = input.substring(1);
            if (remainder.isEmpty()) return base;
            try { return base + Float.parseFloat(remainder);
            } catch (NumberFormatException ex) { return null; }
        }
        try { return Float.parseFloat(input);
        } catch (NumberFormatException ex) { return null; }
    }
}