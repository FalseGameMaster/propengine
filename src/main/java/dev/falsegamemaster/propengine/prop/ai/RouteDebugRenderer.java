package dev.falsegamemaster.propengine.prop.ai;

import dev.falsegamemaster.propengine.util.Transform;
import dev.falsegamemaster.propengine.util.Util;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO (5/16/2026): merge with GraphDebugRenderer
public class RouteDebugRenderer {

    private static final String DEBUG_TAG = "propengine.debug.route";

    private final Map<PropAI.PatrolRouteDefinition, List<Entity>> shownRoutes = new HashMap<>();

    public RouteDebugRenderer(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PropAI.PatrolRouteDefinition route : new ArrayList<>(shownRoutes.keySet())) {
                spawnRouteParticles(route);
            }
        }, 0L, 10L);
    }

    public void showRoute(PropAI.PatrolRouteDefinition route) {
        hideRoute(route);
        List<Entity> spawnedNodes = new ArrayList<>();
        List<String> nodeIds = route.getNodeIds();
        for (String nodeId : nodeIds) {
            Location location = route.graph.requireNode(nodeId).location();
            ItemDisplay nodeDisplay = location.getWorld().spawn(location.clone().add(-0.5, -0.5, -0.5), ItemDisplay.class, display -> {
                display.setItemStack(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS)); // TODO (5/16/2026): replace this with dynamic color
                display.setGlowing(true);
                display.setPersistent(false);
                display.addScoreboardTag(DEBUG_TAG);
                display.setTransformation(new Transform.Builder().scale(new Vector3f(0.3f, 0.3f, 0.3f)).build().bake());
            });
            spawnedNodes.add(nodeDisplay);
        }
        shownRoutes.put(route, spawnedNodes);
    }

    private void spawnRouteParticles(PropAI.PatrolRouteDefinition route) {
        List<String> nodeIds = route.getNodeIds();
        for (int i = 0; i + 1 < nodeIds.size(); i ++) {
            Location from = route.graph.requireNode(nodeIds.get(i)).location();
            Location to = route.graph.requireNode(nodeIds.get(i + 1)).location();
            Util.spawnParticleLine(from, to, route.color);
        }
        if (route.mode == PropAI.PatrolMode.LOOP) {
            Location from = route.graph.requireNode(nodeIds.getLast()).location();
            Location to = route.graph.requireNode(nodeIds.getFirst()).location();
            Util.spawnParticleLine(from, to, route.color);
        }
    }

    public void hideRoute(PropAI.PatrolRouteDefinition route) {
        List<Entity> entities = shownRoutes.remove(route);
        if (entities == null) return;
        for (Entity entity : entities) {
            if (!entity.isDead()) entity.remove();
        }
    }

    public void hideAll() {
        for (PropAI.PatrolRouteDefinition route : new ArrayList<>(shownRoutes.keySet())) {
            hideRoute(route);
        }
    }

}
