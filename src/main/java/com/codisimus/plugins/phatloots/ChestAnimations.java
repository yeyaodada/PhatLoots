package com.codisimus.plugins.phatloots;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public class ChestAnimations {

    public static void openChest(Block block) {
        playChestAction(null, block, true);
    }

    public static void openChest(Player player, Block block) {
        playChestAction(player, block, true);
    }

    public static void closeChest(Block block) {
        playChestAction(null, block, false);
    }

    public static void closeChest(Player player, Block block) {
        playChestAction(player, block, false);
    }

    public static void playChestAction(Player player, Block block, boolean open) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return;
        }

        if (player != null && isMojmapEnvironment()) {
            Location location = chest.getLocation();

            // If we're in a Mojmap environment, we need to use reflection to
            // play the chest animation to just a single player, otherwise fall
            // back to the default behavior. Not as clean but better than nothing.
            playChestActionMojmap(player, location, open);
        } else {
            if (open) {
                chest.open();
            } else {
                chest.close();
            }
        }
    }

    private static boolean isMojmapEnvironment() {
        try {
            Class.forName("net.minecraft.network.protocol.game.ClientboundBlockEventPacket");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void playChestActionMojmap(Player player, Location location, boolean open) {
        // BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        // ((CraftPlayer) player).getHandle().connection.send(new ClientboundBlockEventPacket(pos, Blocks.CHEST, 1, open ? 1 : 0));

        try {
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundBlockEventPacket");
            Constructor<?> packetConstructor = packetClass.getConstructor(
                    Class.forName("net.minecraft.core.BlockPos"),
                    Class.forName("net.minecraft.world.level.block.Block"),
                    int.class,
                    int.class
            );

            Object pos = Class.forName("net.minecraft.core.BlockPos").getConstructor(int.class, int.class, int.class).newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Object blockClass = Class.forName("net.minecraft.world.level.block.Blocks").getField("CHEST").get(null);
            Object packet = packetConstructor.newInstance(pos, blockClass, 1, open ? 1 : 0);

            Object playerHandle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = playerHandle.getClass().getField("connection").get(playerHandle);
            connection.getClass().getMethod("send", Class.forName("net.minecraft.network.protocol.Packet")).invoke(connection, packet);
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException | ClassNotFoundException e) {
            PhatLoots.logger.log(Level.SEVERE, "Failed to play chest animation", e);
        }
    }
}
