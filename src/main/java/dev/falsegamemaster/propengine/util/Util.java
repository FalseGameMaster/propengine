package dev.falsegamemaster.propengine.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {

    public static Entity getEntityByTag(World world, String selectorTag) {
        for (Entity entity : world.getEntities()) {
            if (!entity.getScoreboardTags().contains(selectorTag)) continue;
            return entity;
        }
        return null;
    }

    public static ItemStack getItemWithCustomModelData(Material material, int customModelData) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setCustomModelData(customModelData);
        stack.setItemMeta(meta);
        return stack;
    }

}
