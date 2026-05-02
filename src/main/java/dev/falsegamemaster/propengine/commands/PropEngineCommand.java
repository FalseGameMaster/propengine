package dev.falsegamemaster.propengine.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.*;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.World;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// TODO: rework facing/rotated literals for the /propengine command (4/12/2026)

@SuppressWarnings("UnstableApiUsage")
public final class PropEngineCommand {

    private final PropEnginePlugin propEngine;

    public PropEngineCommand(PropEnginePlugin propEngine) {
        this.propEngine = propEngine;
    }

    public void register() {
        propEngine.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(Commands.literal("propengine").requires(source -> source.getSender().hasPermission("propengine.command.propengine"))
                .then(Commands.literal("spawn")
                .then(buildTypeBranch(PropType.STATIC))
                .then(buildTypeBranch(PropType.DOOR))
                .then(buildTypeBranch(PropType.ELEVATOR))
                .then(buildTypeBranch(PropType.VEHICLE))
                .then(buildTypeBranch(PropType.NPC))
                .then(buildTypeBranch(PropType.MISC))).build());
        });
    }

    private ArgumentBuilder<CommandSourceStack, ?> buildTypeBranch(PropType propType) {
        var nameBranch = Commands.argument("name", StringArgumentType.word())
            .then(buildAtBranch(propType, false))
            .then(Commands.literal("in").then(Commands.argument("dimension", StringArgumentType.word()).suggests(this::suggestWorlds)
            .then(buildAtBranch(propType, true))));
        return Commands.literal(propType.literal())
            .then(Commands.argument("literal", StringArgumentType.word()).suggests((context, builder) -> suggestPropsOfType(propType, builder))
            .then(nameBranch));
    }

    private ArgumentBuilder<CommandSourceStack, ?> buildAtBranch(PropType propType, boolean explicitDimension) {
        var positionBranch = Commands.argument("position", ArgumentTypes.finePosition());
        var facingBranch = Commands.literal("facing")
                .then(Commands.argument("facing", StringArgumentType.word()).suggests(this::suggestCardinals)
                .executes(context -> executeSpawn(context, propType, PropRotationMode.FACING, explicitDimension))
                .then(Commands.argument("data", StringArgumentType.greedyString())
                .executes(context -> executeSpawn(context, propType, PropRotationMode.FACING, explicitDimension))));
        positionBranch.then(facingBranch);
        if (propType.rotationPolicy() == PropRotationPolicy.EULER) {
            var rotatedBranch = Commands.literal("rotated")
                .then(Commands.argument("yaw", StringArgumentType.word())
                .then(Commands.argument("pitch", StringArgumentType.word())
                .then(Commands.argument("roll", StringArgumentType.word())
                .executes(context -> executeSpawn(context, propType, PropRotationMode.ROTATED, explicitDimension))
                .then(Commands.argument("data", StringArgumentType.greedyString())
                .executes(context -> executeSpawn(context, propType, PropRotationMode.ROTATED, explicitDimension))))));
            positionBranch.then(rotatedBranch);
        }
        return Commands.literal("at").then(positionBranch);
    }

    private int executeSpawn(CommandContext<CommandSourceStack> context, PropType propType, PropRotationMode rotationMode, boolean explicitDimension) {
        PropSpawnRequest request = PropSpawnArgumentParser.parse(propEngine, context, propType, rotationMode, explicitDimension);
        if (request == null) return 0;
        Prop.spawn(propEngine, request);
        context.getSource().getSender().sendPlainMessage("Spawned " + request.literal());
        return 1;
    }

    private CompletableFuture<Suggestions> suggestPropsOfType(PropType propType, SuggestionsBuilder builder) {
        for (Map.Entry<String, IPropFactory<? extends Prop>> entry : propEngine.PROP_REGISTRAR.getEntries().entrySet()) {
            String literal = entry.getKey();
            IPropFactory<? extends Prop> factory = entry.getValue();
            if (Objects.equals(Prop.dummy(factory).getPropType(), propType)) builder.suggest(literal);
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestWorlds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (World world : propEngine.getServer().getWorlds()) builder.suggest(world.getName());
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCardinals(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return builder.suggest("north").suggest("east").suggest("south").suggest("west").suggest("n").suggest("e").suggest("s").suggest("w").buildFuture();
    }
}