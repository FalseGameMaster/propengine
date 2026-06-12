package dev.falsegamemaster.propengine.prop.ai;

import dev.falsegamemaster.propengine.prop.Prop;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.BiFunction;

public class PropAI {

    private PropAI() {}

    public enum NavState {
        PATROLLING,
        CHASING,
        INVESTIGATING,
        RETURNING_TO_PATROL,
        ATTACKING
    }

    public record PathNode(String id, Location location, Set<String> tags) {}

    public record PathEdge(String from, String to, double cost) {}

    public static class PathGraph {
        public final String name;
        private final Map<String, PathNode> nodes = new HashMap<>();
        private final Map<String, List<PathEdge>> edges = new HashMap<>();
        public final Color color;

        public PathGraph(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public void addNode(PathNode node) {
            nodes.put(node.id(), node);
            edges.putIfAbsent(node.id(), new ArrayList<>());
        }

        public void addEdge(String from, String to, double cost) {
            requireNode(from);
            requireNode(to);
            edges.computeIfAbsent(from, k -> new ArrayList<>()).add(new PathEdge(from, to, cost));
            edges.computeIfAbsent(to, k -> new ArrayList<>()).add(new PathEdge(to, from, cost));
        }

        public PathNode requireNode(String id) {
            PathNode node = nodes.get(id);
            if (node == null) throw new IllegalArgumentException("Unknown nav node: " + id);
            return node;
        }

        public PathNode findNearestNode(Location location) {
            return nodes.values().stream().min(Comparator.comparingDouble(node -> node.location().distanceSquared(location))).orElse(null);
        }

        public PathNode findNearestNode(Location location, String requiredTag) {
            return nodes.values().stream().filter(node -> node.tags().contains(requiredTag)).min(Comparator.comparingDouble(node -> node.location().distanceSquared(location))).orElse(null);
        }

        public boolean hasEdge(String from, String to) {
            requireNode(from);
            requireNode(to);
            return edges.getOrDefault(from, List.of()).stream().anyMatch(edge -> (edge.from().equals(from) && edge.to().equals(to)) || (edge.from().equals(to) && edge.to().equals(from)));
        }

        public Collection<PathNode> getNodes() {
            return List.copyOf(nodes.values());
        }

        public Collection<PathEdge> getEdges() {
            return edges.values().stream().flatMap(List::stream).filter(edge -> edge.from().compareTo(edge.to()) < 0).toList();
        }
    }

    public enum PatrolMode {
        LOOP,
        PING_PONG
    }

    public static class PatrolRouteDefinition {
        public final String name;
        public final PathGraph graph;
        public final PatrolMode mode;
        @Deprecated private final Set<String> tags;
        private final List<String> nodeIds;
        public final Color color;

        public PatrolRouteDefinition(String name, PathGraph graph, PatrolMode mode, Set<String> tags, Color color, List<String> nodeIds) {
            if (nodeIds == null || nodeIds.isEmpty()) throw new IllegalArgumentException("Patrol route must contain at least one node.");
            this.name = name;
            this.graph = graph;
            this.mode = mode;
            this.tags = tags;
            this.nodeIds = nodeIds;
            this.color = color;
            validate(graph);
        }

        public PatrolRouteDefinition(String name, PathGraph graph, PatrolMode mode, Set<String> tags, Color color, String[] nodeIds) {
            this(name, graph, mode, tags, color, new ArrayList<>(List.of(nodeIds)));
        }

        public PatrolRouteDefinition(String name, PathGraph graph, PatrolMode mode, Set<String> tags, Color color, String firstNodeId, String... nodeIds) {
            this(name, graph, mode, tags, color, ((BiFunction<String, String[], List<String>>) (firstNodeId1, nodeIds1) -> {
                List<String> nodeIdList = new ArrayList<>();
                nodeIdList.add(firstNodeId1);
                nodeIdList.addAll(Arrays.asList(nodeIds1));
                return nodeIdList;
            }).apply(firstNodeId, nodeIds));
        }

        public Set<String> getTags() {
            return Set.copyOf(tags);
        }

        public List<String> getNodeIds() {
            return List.copyOf(nodeIds);
        }

        public void validate(PathGraph graph) {
            for (String nodeId : nodeIds) {
                graph.requireNode(nodeId);
            }
            for (int i = 0; i < nodeIds.size() - 1; i ++) {
                requireEdge(graph, nodeIds.get(i), nodeIds.get(i + 1));
            }
            if (mode == PatrolMode.LOOP && nodeIds.size() > 1) {
                requireEdge(graph, nodeIds.getLast(), nodeIds.getFirst());
            }
        }

        private void requireEdge(PathGraph graph, String from, String to) {
            if (!graph.hasEdge(from, to)) {
                throw new IllegalArgumentException("Patrol route contains disconnected nodes: " + from + " -> " + to);
            }
        }
    }

    public static class PatrolRouteInstance {
        private final PatrolRouteDefinition definition;
        private int currentIndex = 0;
        private int direction = 1;

        public PatrolRouteInstance(PatrolRouteDefinition definition) {
            this.definition = definition;
        }

        public String getTargetNodeId() {
            return definition.nodeIds.get(currentIndex);
        }

        public void advance() {
            if (definition.nodeIds.size() == 1) return;
            if (definition.mode == PatrolMode.LOOP) {
                currentIndex = (currentIndex + 1) % definition.nodeIds.size();
                return;
            }
            int nextIndex = currentIndex + direction;
            direction = nextIndex >= definition.nodeIds.size() ? -1 : nextIndex < 0 ? 1 : direction;
            currentIndex += direction;
        }
    }

    public static class Navigator {
        private final Prop prop;
        private Location target;
        private double blocksPerSecond;
        private boolean reachedTarget = true;

        public Navigator(Prop prop, double blocksPerSecond) {
            this.prop = prop;
            this.blocksPerSecond = blocksPerSecond;
        }

        public void setTarget(Location target) {
            this.target = target == null ? null : target.clone();
            this.reachedTarget = target == null;
        }

        public boolean hasReachedTarget() {
            return reachedTarget;
        }

        public void setBlocksPerSecond(double blocksPerSecond) {
            this.blocksPerSecond = blocksPerSecond;
        }

        public void stop() {
            target = null;
            reachedTarget = true;
        }

        public void tick() {
            if (target == null) {
                reachedTarget = true;
                return;
            }
            Location current = prop.getLocation();
            Vector delta = target.toVector().subtract(current.toVector());
            double distance = delta.length();
            if (distance <= 0.05) {
                prop.teleportTo(target);
                reachedTarget = true;
                return;
            }
            double blocksPerTick = blocksPerSecond / 20.0;
            Vector step = delta.normalize().multiply(Math.min(blocksPerTick, distance));
            float yaw = calculateYaw(step, current.getYaw());
            prop.moveBy(step, yaw, current.getPitch());
            reachedTarget = false;
        }

        private float calculateYaw(Vector movement, float currentYaw) {
            if (movement.lengthSquared() == 0) return currentYaw;
            float targetYaw = (float) Math.toDegrees(Math.atan2(-movement.getX(), movement.getZ()));
            return nearestYaw(currentYaw, targetYaw);
        }

        private float nearestYaw(float currentYaw, float targetYaw) {
            float delta = wrapDegrees(targetYaw - currentYaw);
            return currentYaw + delta;
        }

        private float wrapDegrees(float degrees) {
            degrees %= 360.0f;
            if (degrees >= 180.0f) degrees -= 360.0f;
            if (degrees < -180.0f) degrees += 360.0f;
            return degrees;
        }

//        @Deprecated
//        private float calculateYawOLD(Vector movement, float fallbackYaw) {
//            if (movement.lengthSquared() == 0) return fallbackYaw;
//            return (float) Math.toDegrees(Math.atan2(-movement.getX(), movement.getZ()));
//        }
    }

    public static class Controller {
        private final Prop prop;
        private final PathGraph pathGraph;
        private final PatrolRouteInstance patrolRouteInstance;
        private final Navigator navigator;

        private NavState state = NavState.PATROLLING;

        public Controller(Prop prop, PathGraph pathGraph, PatrolRouteInstance patrolRouteInstance, double patrolSpeedBlocksPerSecond) {
            this.prop = prop;
            this.pathGraph = pathGraph;
            this.patrolRouteInstance = patrolRouteInstance;
            this.navigator = new Navigator(prop, patrolSpeedBlocksPerSecond);
            setTargetToCurrentPatrolNode();
        }

        public Prop getProp() {
            return prop;
        }

        public NavState getNavigationState() {
            return state;
        }

        public void setNavigationState(NavState state) {
            this.state = state;
        }

        public Navigator getNavigator() {
            return navigator;
        }

        public void tick() {
            switch (state) {
                case PATROLLING -> tickPatrol();
                case CHASING -> tickChase();
                case INVESTIGATING -> tickInvestigate();
                case RETURNING_TO_PATROL -> tickReturnToPatrol();
                case ATTACKING -> tickAttack();
            }
        }

        private void tickPatrol() {
            navigator.tick();
            if (!navigator.hasReachedTarget()) return;
            patrolRouteInstance.advance();
            setTargetToCurrentPatrolNode();
        }

        private void tickChase() {
            // Later:
            // 1. Update target to player location.
            // 2. If close enough, switch to ATTACKING.
            // 3. If player lost, switch to INVESTIGATING.
            navigator.tick();
        }

        private void tickInvestigate() {
            // Later:
            // Move to last-known-player location.
            // If reached and no player seen, switch to RETURNING_TO_PATROL.
            navigator.tick();
        }

        private void tickReturnToPatrol() {
            // Later:
            // Pathfind to nearest patrol node/homing node.
            // When reached, switch back to PATROLLING.
            navigator.tick();
        }

        private void tickAttack() {
            navigator.stop();

            // Later:
            // play attack animation
            // damage/kill target
            // then RETURNING_TO_PATROL or CHASING
        }

        private void setTargetToCurrentPatrolNode() {
            PathNode targetNode = pathGraph.requireNode(patrolRouteInstance.getTargetNodeId());
            navigator.setTarget(targetNode.location());
        }
    }

    public static class PathRegistrar {
        private final Map<String, PropAI.PathGraph> graphs = new HashMap<>();
        private final Map<String, PropAI.PatrolRouteDefinition> routes = new HashMap<>();

        public void register(PropAI.PathGraph graph) {
            graphs.put(graph.name, graph);
        }

        public void register(PropAI.PatrolRouteDefinition route) {
            routes.put(route.name, route);
        }

        public Collection<PropAI.PathGraph> getGraphs() {
            return graphs.values();
        }

        public Collection<PropAI.PatrolRouteDefinition> getRoutes() {
            return routes.values();
        }

        public PropAI.PathGraph getGraph(String name) {
            return graphs.get(name);
        }

        public PropAI.PatrolRouteDefinition getRoute(String name) {
            return routes.get(name);
        }
    }

}
