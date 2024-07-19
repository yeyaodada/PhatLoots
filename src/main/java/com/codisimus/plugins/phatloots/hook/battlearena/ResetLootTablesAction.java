package com.codisimus.plugins.phatloots.hook.battlearena;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import org.battleplugins.arena.Arena;
import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.competition.Competition;
import org.battleplugins.arena.competition.LiveCompetition;
import org.battleplugins.arena.competition.map.options.Bounds;
import org.battleplugins.arena.event.action.EventAction;
import org.battleplugins.arena.resolver.Resolvable;
import org.bukkit.Location;

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
    }
}
