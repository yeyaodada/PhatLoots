package com.codisimus.plugins.phatloots.hook.battlearena;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import org.battleplugins.arena.Arena;
import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.competition.Competition;
import org.battleplugins.arena.competition.LiveCompetition;
import org.battleplugins.arena.competition.map.MapType;
import org.battleplugins.arena.competition.map.options.Bounds;
import org.battleplugins.arena.event.action.EventAction;
import org.battleplugins.arena.options.types.BooleanArenaOption;
import org.battleplugins.arena.resolver.Resolvable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Resets all loot tables in an Arena.
 */
public class ResetLootTablesAction extends EventAction {

    public ResetLootTablesAction(Map<String, String> params) {
        super(params);
    }

    @Override
    public void call(ArenaPlayer arenaPlayer, Resolvable resolvable) {
    }

    @Override
    public void postProcess(Arena arena, Competition<?> competition, Resolvable resolvable) {
        if (!(competition instanceof LiveCompetition<?> liveCompetition)) {
            return;
        }

        Bounds bounds = liveCompetition.getMap().getBounds();
        if (bounds == null) {
            return;
        }

        if (liveCompetition.getMap().getType() == MapType.STATIC) {
            for (PhatLootChest chest : PhatLootChest.getChests()) {
                if (!chest.isInWorld(liveCompetition.getMap().getWorld())) {
                    continue;
                }

                Location location = chest.getLocation();
                if (!bounds.isInside(location)) {
                    continue;
                }

                for (PhatLoot phatLoot : chest.getLinkedPhatLoots()) {
                    phatLoot.reset(chest.getBlock());
                }
            }
        } else {
            // For dynamic maps, we don't want to actually set a loot table since
            // the world is only dynamic and will be removed. Instead, locate all the
            // loot tables in the parent world, and if we are dealing with a container
            //  (i.e. a chest), we roll for the loot and populate the container
            for (PhatLootChest chest : PhatLootChest.getChests()) {
                if (!chest.isInWorld(liveCompetition.getMap().getParentWorld())) {
                    continue;
                }

                Location parentLocation = chest.getLocation();
                if (!bounds.isInside(parentLocation)) {
                    continue;
                }

                Location mapLocation = new Location(liveCompetition.getMap().getWorld(), parentLocation.getX(), parentLocation.getY(), parentLocation.getZ());
                Block block = mapLocation.getBlock();
                BlockState state = block.getState();
                if ((!(state instanceof InventoryHolder inventoryHolder))) {
                    continue;
                }

                Inventory inventory = inventoryHolder.getInventory();

                for (PhatLoot phatLoot : chest.getLinkedPhatLoots()) {
                    LootBundle bundle = phatLoot.rollForLoot();
                    for (ItemStack item : bundle.getItemList()) {
                        inventory.addItem(item);
                    }
                }

                boolean shuffleLoot = liveCompetition.option(BattleArenaListener.SHUFFLE_LOOT).map(BooleanArenaOption::isEnabled).orElse(false);
                if (shuffleLoot) {
                    List<ItemStack> contents = Arrays.asList(inventory.getContents());
                    Collections.shuffle(contents);
                    inventory.setContents(contents.toArray(ItemStack[]::new));
                }
            }
        }
    }
}
