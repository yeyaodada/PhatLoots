package com.codisimus.plugins.phatloots.hook.battlearena;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.PhatLoots;
import org.battleplugins.arena.Arena;
import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.competition.Competition;
import org.battleplugins.arena.competition.LiveCompetition;
import org.battleplugins.arena.competition.map.options.Bounds;
import org.battleplugins.arena.event.action.EventAction;
import org.battleplugins.arena.resolver.Resolvable;
import org.bukkit.Location;
import org.bukkit.block.Block;

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

        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++) {
            for (int y = bounds.getMinY(); y <= bounds.getMaxY(); y++) {
                for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                    Location location = new Location(liveCompetition.getMap().getWorld(), x, y, z);
                    Block block = location.getBlock();
                    if (!PhatLootChest.isPhatLootChest(block)) {
                        continue;
                    }

                    PhatLootChest chest = PhatLootChest.getChest(location);
                    for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
                        if (phatLoot.containsChest(chest)) {
                            phatLoot.reset(block);
                        }
                    }
                }
            }
        }
    }
}
