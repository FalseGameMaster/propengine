package dev.falsegamemaster.propengine.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.*;
import dev.falsegamemaster.propengine.prop.animation.PropAnimation;
import dev.falsegamemaster.propengine.registration.IPropFactory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.*;
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
                .then(LoadUnloadDestroyBranch.createLoadBranch())
                .then(LoadUnloadDestroyBranch.createUnloadBranch())
                .then(LoadUnloadDestroyBranch.createDestroyBranch())
                .then(SpawnBranch.create())
                .then(AnimationBranch.create())
                .then(GraphShowHideBranch.create())
                .then(RouteShowHideBranch.create())
                .build());
        });
    }

    public static class LoadUnloadDestroyBranch {
        protected static LiteralArgumentBuilder<CommandSourceStack> createLoadBranch() {
            PropAction action = PropAction.LOAD;
            return Commands.literal(action.toString().toLowerCase())
                .then(Commands.literal("from")
                .then(Commands.literal("tags")
                .then(Commands.argument("tag expression", StringArgumentType.greedyString())
                .executes(context -> executeFromTags(context, action)))));
        }

        protected static LiteralArgumentBuilder<CommandSourceStack> createUnloadBranch() {
            return buildUnloadDestroyBranch(PropAction.UNLOAD);
        }

        protected static LiteralArgumentBuilder<CommandSourceStack> createDestroyBranch() {
            return buildUnloadDestroyBranch(PropAction.DESTROY);
        }

        private static LiteralArgumentBuilder<CommandSourceStack> buildUnloadDestroyBranch(PropAction action) {
            return Commands.literal(action.toString().toLowerCase())
                .then(Commands.literal("from")
                .then(Commands.literal("tags")
                .then(Commands.argument("tag expression", StringArgumentType.greedyString())
                .executes(context -> executeFromTags(context, action))))
                .then(Commands.literal("entities")
                .then(Commands.argument("entities", ArgumentTypes.entities())
                .executes(context -> executeFromEntities(context, action)))));
        }

        private static int executeFromTags(CommandContext<CommandSourceStack> context, PropAction action) {
            String tagExpression = StringArgumentType.getString(context, "tag expression");
            List<Prop> props = Prop.getPropsMatchingTags(tagExpression);
            if (props.isEmpty()) {
                context.getSource().getSender().sendPlainMessage("No props found matching tag expression: " + tagExpression);
                return 0;
            }
            applyAction(props, action);
            context.getSource().getSender().sendPlainMessage(action.pastTense + " " + props.size() + " prop(s) matching '" + tagExpression + "'");
            return props.size();
        }

        private static int executeFromEntities(CommandContext<CommandSourceStack> context, PropAction action) {
            try {
                EntitySelectorArgumentResolver resolver = context.getArgument("entities", EntitySelectorArgumentResolver.class);
                Collection<Entity> entities = resolver.resolve(context.getSource());
                Map<String, Prop> props = new HashMap<>();
                for (Entity entity : entities) {
                    Prop prop = Prop.getPropFromEntity(entity);
                    if (prop != null) props.putIfAbsent(prop.getKey(), prop);
                }
                if (props.isEmpty()) {
                    context.getSource().getSender().sendPlainMessage("No PropEngine props found from selected entities.");
                    return 0;
                }
                applyAction(props.values(), action);
                context.getSource().getSender().sendPlainMessage(action.pastTense + " " + props.size() + " prop(s) from selected entities.");
                return props.size();
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        private static void applyAction(Collection<Prop> props, PropAction action) {
            for (Prop prop : props) {
                switch (action) {
                    case LOAD -> prop.load();
                    case UNLOAD -> prop.unload();
                    case DESTROY -> prop.destroy();
                }
            }
        }

        private enum PropAction {
            LOAD("Loaded"),
            UNLOAD("Unloaded"),
            DESTROY("destroyed");

            private final String pastTense;

            PropAction(String pastTense) {
                this.pastTense = pastTense;
            }
        }
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
                .then(buildAtBranch(propType)))));
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildAtBranch(PropType propType) {
            var positionBranch = Commands.argument("position", ArgumentTypes.finePosition());
            var facingBranch = Commands.literal("facing")
                .then(Commands.argument("facing", StringArgumentType.word()).suggests(SpawnBranch::suggestCardinals)
                .executes(context -> executeSpawn(context, propType, PropRotationMode.FACING))
                .then(Commands.argument("data", StringArgumentType.greedyString())
                .executes(context -> executeSpawn(context, propType, PropRotationMode.FACING))));
            positionBranch.then(facingBranch);
            if (propType.rotationPolicy() == PropRotationPolicy.EULER) {
                var rotatedBranch = Commands.literal("rotated")
                    .then(Commands.argument("yaw", StringArgumentType.word())
                    .then(Commands.argument("pitch", StringArgumentType.word())
                    .then(Commands.argument("roll", StringArgumentType.word())
                    .executes(context -> executeSpawn(context, propType, PropRotationMode.ROTATED))
                    .then(Commands.argument("data", StringArgumentType.greedyString())
                    .executes(context -> executeSpawn(context, propType, PropRotationMode.ROTATED))))));
                positionBranch.then(rotatedBranch);
            }
            return Commands.literal("at").then(positionBranch);
        }

        private static int executeSpawn(CommandContext<CommandSourceStack> context, PropType propType, PropRotationMode rotationMode) {
            PropEnginePlugin plugin = PropEnginePlugin.getInstance();
            Prop.SpawnRequest request = PropSpawnArgumentParser.parse(plugin, context, propType, rotationMode);
            if (request == null) return 0;
            Prop.spawnNew(plugin, request);
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

    public static class AnimationBranch {
        protected static LiteralArgumentBuilder<CommandSourceStack> create() {
            var animationBranch = Commands.literal("animation");
            return animationBranch.then(buildPlayBranch()).then(buildPauseBranch());
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildPlayBranch() {
            return Commands.literal("play")
                .then(Commands.literal("for")
                .then(Commands.argument("tag expression", StringArgumentType.word())
                .then(Commands.argument("animation", StringArgumentType.word())
                .suggests(animationSuggestions())
                .executes(context -> {
                    String tagExpression = StringArgumentType.getString(context, "tag expression");
                    String animation = StringArgumentType.getString(context, "animation");
                    List<Prop> props = Prop.getPropsMatchingTags(tagExpression);
                    if (props.isEmpty()) {
                        context.getSource().getSender().sendPlainMessage("No spawned props found matching tag expression: " + tagExpression);
                        return 0;
                    }
                    int played = 0;
                    for (Prop prop : props) {
                        if (!prop.getRegisteredAnimations().containsKey(animation)) continue;
                        prop.playAnimation(animation);
                        played ++;
                    }
                    if (played == 0) {
                        context.getSource().getSender().sendPlainMessage("No props matching tag expression '" + tagExpression + "' have animation: " + animation);
                        return 0;
                    }
                    context.getSource().getSender().sendPlainMessage("Playing animation '" + animation + "' for props matching tag expression '" + tagExpression + "' (" + played + " prop(s))");
                    return played;
                }))));
        }

        private static ArgumentBuilder<CommandSourceStack, ?> buildPauseBranch() {
            return Commands.literal("pause")
                .then(Commands.literal("for")
                .then(Commands.argument("tag expression", StringArgumentType.word())
                .executes(context -> {
                    String tagExpression = StringArgumentType.getString(context, "tag expression");
                    List<Prop> props = Prop.getPropsMatchingTags(tagExpression);
                    for (Prop prop : props) {
                        prop.stopAnimation();
                    }
                    context.getSource().getSender().sendPlainMessage("Stopping animations for props matching tag expression '" + tagExpression + "' (" + props.size() + " prop(s))");
                    return 1;
                })));
        }

        private static SuggestionProvider<CommandSourceStack> animationSuggestions() {
            return (context, builder) -> {
                String inputBeforeCursor = builder.getInput().substring(0, builder.getStart()).trim();
                String[] parts = inputBeforeCursor.split("\\s+");
                if (parts.length < 5) return builder.buildFuture();
                String tagExpression = parts[4];
                List<Prop> props = Prop.getPropsMatchingTags(tagExpression);
                for (Prop prop : props) {
                    Map<String, PropAnimation<?>> registeredAnimations = prop.getRegisteredAnimations();
                    for (String animationName : registeredAnimations.keySet()) {
                        builder.suggest(animationName);
                    }
                }
                return builder.buildFuture();
            };
        }
    }

    public static class GraphCreateRemoveBranch {

    }

    public static class GraphListBranch {

    }

    public static class GraphShowHideBranch {
        protected static LiteralArgumentBuilder<CommandSourceStack> create() {
            var graphBranch = Commands.literal("graph");
            return graphBranch.then(buildShowBranch()).then(buildHideBranch());
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
            var routeBranch = Commands.literal("route");
            return routeBranch.then(buildShowBranch()).then(buildHideBranch());
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