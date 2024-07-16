package com.codisimus.plugins.phatloots.hook.battlearena;

import com.codisimus.plugins.phatloots.PhatLoots;
import org.battleplugins.arena.event.action.EventActionType;
import org.bukkit.plugin.PluginManager;

public class BattleArenaManager {

    public BattleArenaManager(PhatLoots plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (pluginManager.getPlugin("BattleArena") != null) {
            pluginManager.registerEvents(new BattleArenaListener(), plugin);

            // Register the reset loot tables action
            EventActionType.create("reset-loot-tables", ResetLootTablesAction.class, ResetLootTablesAction::new);
            EventActionType.create("give-loot-bag", GiveLootBagAction.class, GiveLootBagAction::new);
        }
    }
}
