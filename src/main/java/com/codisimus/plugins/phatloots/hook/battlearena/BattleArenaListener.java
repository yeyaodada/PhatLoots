package com.codisimus.plugins.phatloots.hook.battlearena;

import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.events.PlayerLootEvent;
import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.BattleArena;
import org.battleplugins.arena.competition.map.options.Bounds;
import org.battleplugins.arena.options.ArenaOptionType;
import org.battleplugins.arena.options.types.BooleanArenaOption;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class BattleArenaListener implements Listener {
    private static final ArenaOptionType<BooleanArenaOption> SHUFFLE_LOOT = ArenaOptionType.create("shuffle-loot", BooleanArenaOption::new);

    @EventHandler
    public void onLoot(PlayerLootEvent event) {
        if (!BattleArena.getInstance().isInArena(event.getLooter())) {
            return;
        }

        ArenaPlayer player = ArenaPlayer.getArenaPlayer(event.getLooter());
        PhatLootChest chest = event.getChest();
        if (chest == null) {
            return;
        }

        Block block = chest.getBlock();
        Bounds bounds = player.getCompetition().getMap().getBounds();
        if (bounds == null || !bounds.isInside(block.getLocation())) {
            return;
        }

        boolean shuffleLoot = player.getCompetition().option(SHUFFLE_LOOT).map(BooleanArenaOption::isEnabled).orElse(false);
        if (shuffleLoot) {
            event.setShuffleLoot(true);
        }
    }
}
