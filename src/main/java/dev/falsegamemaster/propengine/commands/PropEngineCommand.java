package dev.falsegamemaster.propengine.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
                .then(SpawnBranch.create())
                .then(GraphShowHideBranch.create())
                .then(RouteShowHideBranch.create())
                .build());
        });
    }

    public static class SpawnBranch {
        protected static LiteralArgumentBuilder<CommandSourceStack> create() {
            var spawnBranch = Commands.literal("spawn");
            for (PropType propType : PropEnginePlugin.getInstance().propTypeRegistrar.getEntries().values()) {
                spawnBranch.then(buildTypeBranch(propType));
            }
            return spawnBranch;
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildTypeBranch(PropType propType) {
            return Commands.literal(propType.literal())
                .then(Commands.argument("literal", StringArgumentType.word()).suggests((context, builder) -> suggestPropsOfType(propType, builder))
                .then(Commands.literal("in").then(Commands.argument("dimension", StringArgumentType.word()).suggests(SpawnBranch::suggestWorlds)
                .then(buildAtBranch(propType, true)))));
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildAtBranch(PropType propType, boolean explicitDimension) {
            var positionBranch = Commands.argument("position", ArgumentTypes.finePosition());
            var facingBranch = Commands.literal("facing")
                .then(Commands.argument("facing", StringArgumentType.word()).suggests(SpawnBranch::suggestCardinals)
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

        private static int executeSpawn(CommandContext<CommandSourceStack> context, PropType propType, PropRotationMode rotationMode, boolean explicitDimension) {
            PropEnginePlugin plugin = PropEnginePlugin.getInstance();
            PropSpawnRequest request = PropSpawnArgumentParser.parse(plugin, context, propType, rotationMode, explicitDimension);
            if (request == null) return 0;
            Prop.spawn(plugin, request);
            context.getSource().getSender().sendPlainMessage("Spawned " + request.literal());
            return 1;
        }

        private static CompletableFuture<Suggestions> suggestPropsOfType(PropType propType, SuggestionsBuilder builder) {
            for (Map.Entry<String, IPropFactory<? extends Prop>> entry : PropEnginePlugin.getInstance().propRegistrar.getEntries().entrySet()) {
                String literal = entry.getKey();
                IPropFactory<? extends Prop> factory = entry.getValue();
                if (Objects.equals(Prop.dummy(factory).getPropType(), propType)) builder.suggest(literal);
            }
            return builder.buildFuture();
        }

        private static CompletableFuture<Suggestions> suggestWorlds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            for (World world : PropEnginePlugin.getInstance().getServer().getWorlds()) builder.suggest(world.getName());
            return builder.buildFuture();
        }

        private static CompletableFuture<Suggestions> suggestCardinals(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            return builder.suggest("north").suggest("east").suggest("south").suggest("west").suggest("n").suggest("e").suggest("s").suggest("w").buildFuture();
        }
    }

    public static class GraphCreateRemoveBranch {

    }

    public static class GraphListBranch {

    }

    public static class GraphShowHideBranch {
        protected static LiteralArgumentBuilder<CommandSourceStack> create() {
            var pathBranch = Commands.literal("graph");
            return pathBranch.then(buildShowBranch()).then(buildHideBranch());
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildShowBranch() {
            return Commands.literal("show")
                .then(Commands.argument("graph", StringArgumentType.greedyString())
                .suggests(graphSuggestions())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "graph");
                    if (name.equals("*")) {
                        for (var graph : PropEnginePlugin.getInstance().pathRegistrar.getGraphs()) {
                            PropEnginePlugin.getInstance().getGraphDebugRenderer().showGraph(graph);
                        }
                        ctx.getSource().getSender().sendMessage("Showing all graphs.");
                        return 1;
                    }
                    var graph = PropEnginePlugin.getInstance().pathRegistrar.getGraph(name);
                    if (graph == null) {
                        ctx.getSource().getSender().sendMessage("Unknown graph: " + name);
                        return 0;
                    }
                    PropEnginePlugin.getInstance().getGraphDebugRenderer().showGraph(graph);
                    ctx.getSource().getSender().sendMessage("Showing graph: " + name);
                    return 1;
                }));
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildHideBranch() {
            return Commands.literal("hide")
                .then(Commands.argument("graph", StringArgumentType.greedyString())
                .suggests(graphSuggestions())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "graph");
                    if (name.equals("*")) {
                        PropEnginePlugin.getInstance().getGraphDebugRenderer().hideAll();
                        ctx.getSource().getSender().sendMessage("Hiding all graphs.");
                        return 1;
                    }
                    var graph = PropEnginePlugin.getInstance().pathRegistrar.getGraph(name);
                    if (graph == null) {
                        ctx.getSource().getSender().sendMessage("Unknown graph: " + name);
                        return 0;
                    }
                    PropEnginePlugin.getInstance().getGraphDebugRenderer().hideGraph(graph);
                    ctx.getSource().getSender().sendMessage("Hiding graph: " + name);
                    return 1;
                }));
        }

        private static SuggestionProvider<CommandSourceStack> graphSuggestions() {
            return (ctx, builder) -> {
                builder.suggest("*");
                for (var graph : PropEnginePlugin.getInstance().pathRegistrar.getGraphs()) {
                    builder.suggest(graph.name);
                }
                return builder.buildFuture();
            };
        }
    }

    public static class RouteShowHideBranch {
        protected static LiteralArgumentBuilder<CommandSourceStack> create() {
            var pathBranch = Commands.literal("route");
            return pathBranch.then(buildShowBranch()).then(buildHideBranch());
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildShowBranch() {
            return Commands.literal("show")
                .then(Commands.argument("route", StringArgumentType.greedyString())
                .suggests(routeSuggestions())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "route");
                    if (name.equals("*")) {
                        for (var route : PropEnginePlugin.getInstance().pathRegistrar.getRoutes()) {
                            PropEnginePlugin.getInstance().getRouteDebugRenderer().showRoute(route);
                        }
                        ctx.getSource().getSender().sendMessage("Showing all routes.");
                        return 1;
                    }
                    var route = PropEnginePlugin.getInstance().pathRegistrar.getRoute(name);
                    if (route == null) {
                        ctx.getSource().getSender().sendMessage("Unknown route: " + name);
                        return 0;
                    }
                    PropEnginePlugin.getInstance().getRouteDebugRenderer().showRoute(route);
                    ctx.getSource().getSender().sendMessage("Showing route: " + name);
                    return 1;
                }));
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildHideBranch() {
            return Commands.literal("hide")
                .then(Commands.argument("route", StringArgumentType.greedyString())
                .suggests(routeSuggestions())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "route");
                    if (name.equals("*")) {
                        PropEnginePlugin.getInstance().getRouteDebugRenderer().hideAll();
                        ctx.getSource().getSender().sendMessage("Hiding all routes.");
                        return 1;
                    }
                    var route = PropEnginePlugin.getInstance().pathRegistrar.getRoute(name);
                    if (route == null) {
                        ctx.getSource().getSender().sendMessage("Unknown route: " + name);
                        return 0;
                    }
                    PropEnginePlugin.getInstance().getRouteDebugRenderer().hideRoute(route);
                    ctx.getSource().getSender().sendMessage("Hiding route: " + name);
                    return 1;
                }));
        }

        private static SuggestionProvider<CommandSourceStack> routeSuggestions() {
            return (ctx, builder) -> {
                builder.suggest("*");
                for (var route : PropEnginePlugin.getInstance().pathRegistrar.getRoutes()) {
                    builder.suggest(route.name);
                }
                return builder.buildFuture();
            };
        }
    }

    public static class RouteLinkUnlinkBranch {

    }

}