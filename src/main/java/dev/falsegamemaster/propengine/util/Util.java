package dev.falsegamemaster.propengine.util;

import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.nms.api.CommandCaptureResult;
import io.papermc.paper.brigadier.NullCommandSender;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {

    private Util() {}

    public static class Entity {
        private Entity() {}

        public static org.bukkit.entity.Entity getEntityByTag(World world, String selectorTag) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (!entity.getScoreboardTags().contains(selectorTag)) continue;
                return entity;
            }
            return null;
        }

        public static List<org.bukkit.entity.Entity> getEntitiesByTag(World world, String selectorTag) {
            List<org.bukkit.entity.Entity> entities = new ArrayList<>();
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains(selectorTag)) entities.add(entity);
            }
            return entities;
        }
    }

    public static class Item {
        private Item() {}

        public static ItemStack getItemWithCustomModelData(Material material, int customModelData) {
            ItemStack stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) return stack;
            meta.setCustomModelData(customModelData);
            stack.setItemMeta(meta);
            return stack;
        }
    }

    public static class InGameDebug {
        private InGameDebug() {}

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

    public static class Data {
        private Data() {}

        public static CompoundTag getTagFromMinecraftStorage(String databaseName, String tableName) {
            CommandCaptureResult result = PropEnginePlugin.getInstance().getNms().dispatchCommandCapturingOutput(NullCommandSender.INSTANCE, "data get storage " + databaseName + " " + tableName);
            List<String> messages = result.messages();
            assert messages.size() == 1 : "/data command returned no output. Does this database exist?";
            String message = messages.getFirst();
            int nbtStringStartIndex = message.indexOf('{');
            assert nbtStringStartIndex != -1 : "Cannot parse NBT data from message string: missing '{' character.";
            try {
                Tag<?> tag = SNBTUtil.fromSNBT(message.substring(nbtStringStartIndex));
                assert tag instanceof CompoundTag : "Parsed NBT data from message string is not a CompoundTag. I have no idea how you managed to break it that badly... good job. Have a smiley face sticker.";
                return (CompoundTag) tag;
            } catch (IOException e) {
                throw new RuntimeException("Exception parsing NBT data from message string: ", e);
            }
        }
    }

}
