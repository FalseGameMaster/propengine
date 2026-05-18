package dev.falsegamemaster.propengine.util;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static Entity getEntityByTag(World world, String selectorTag) {
        for (Entity entity : world.getEntities()) {
            if (!entity.getScoreboardTags().contains(selectorTag)) continue;
            return entity;
        }
        return null;
    }

    public static List<Entity> getEntitiesByTag(World world, String selectorTag) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            if (entity.getScoreboardTags().contains(selectorTag)) entities.add(entity);
        }
        return entities;
    }

    public static ItemStack getItemWithCustomModelData(Material material, int customModelData) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setCustomModelData(customModelData);
        stack.setItemMeta(meta);
        return stack;
    }

    public static void spawnParticleLine(Location from, Location to, Color color) {
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) return;
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.2f);
        Vector delta = to.toVector().subtract(from.toVector());
        double length = delta.length();
        if (length <= 0.001) return;
        Vector direction = delta.normalize();
        double spacing = 0.25;
        int steps = Math.max(1, (int) Math.ceil(length / spacing));
        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(direction.clone().multiply(Math.min(i * spacing, length)));
            world.spawnParticle(Particle.DUST, point.clone().add(-0.5, -0.5, -0.5), 1, 0, 0, 0, 0, dust);
        }
    }

}
