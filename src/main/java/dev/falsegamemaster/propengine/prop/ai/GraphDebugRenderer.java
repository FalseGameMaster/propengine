package dev.falsegamemaster.propengine.prop.ai;

import dev.falsegamemaster.propengine.util.Transform;
import dev.falsegamemaster.propengine.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO (5/16/2026): merge with RouteDebugRenderer
public class GraphDebugRenderer {

    private static final String DEBUG_TAG = "propengine.debug.graph";

    private final Map<PropAI.PathGraph, List<Entity>> shownGraphs = new HashMap<>();

    public GraphDebugRenderer(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PropAI.PathGraph graph : new ArrayList<>(shownGraphs.keySet())) {
                spawnGraphParticles(graph);
            }
        }, 0L, 10L);
    }

    public void showGraph(PropAI.PathGraph graph) {
        hideGraph(graph);
        List<Entity> spawnedNodes = new ArrayList<>();
        for (PropAI.PathNode node : graph.getNodes()) {
            Location location = node.location();
            ItemDisplay nodeDisplay = location.getWorld().spawn(location.clone().add(-0.5, -0.5, -0.5), ItemDisplay.class, display -> {
                display.setItemStack(new ItemStack(Material.ORANGE_STAINED_GLASS)); // TODO (5/16/2026): replace this with dynamic color
                display.setGlowing(true);
                display.setPersistent(false);
                display.addScoreboardTag(DEBUG_TAG);
                display.setTransformation(new Transform.Builder().scale(new Vector3f(0.3f, 0.3f, 0.3f)).build().bake());
            });
            spawnedNodes.add(nodeDisplay);
        }
        shownGraphs.put(graph, spawnedNodes);
    }

    private void spawnGraphParticles(PropAI.PathGraph graph) {
        for (PropAI.PathEdge edge : graph.getEdges()) {
            Location from = graph.requireNode(edge.from()).location();
            Location to = graph.requireNode(edge.to()).location();
            Util.InGameDebug.spawnParticleLine(from, to, graph.color);
        }
    }

    public void hideGraph(PropAI.PathGraph route) {
        List<Entity> entities = shownGraphs.remove(route);
        if (entities == null) return;
        for (Entity entity : entities) {
            if (!entity.isDead()) entity.remove();
        }
    }

    public void hideAll() {
        for (PropAI.PathGraph route : new ArrayList<>(shownGraphs.keySet())) {
            hideGraph(route);
        }
    }

}
