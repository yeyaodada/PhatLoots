package com.codisimus.plugins.phatloots.hook.battlearena;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.config.ItemStackParser;
import org.battleplugins.arena.config.ParseException;
import org.battleplugins.arena.event.action.EventAction;
import org.battleplugins.arena.resolver.Resolvable;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Gives a loot bag to the player.
 */
public class GiveLootBagAction extends EventAction {
    private static final String ITEM_KEY = "item";
    private static final String SLOT_KEY = "slot";
    private static final String LOOT_TABLE_KEY = "loot-table";

    public GiveLootBagAction(Map<String, String> params) {
        super(params, ITEM_KEY, LOOT_TABLE_KEY);
    }

    @Override
    public void call(ArenaPlayer arenaPlayer, Resolvable resolvable) {
        String item = this.get(ITEM_KEY);
        String phatLoot = this.get(LOOT_TABLE_KEY);
        try {
            ItemStack itemStack = ItemStackParser.deserializeSingular(item);
            PhatLoot lootTable = PhatLootsAPI.getPhatLoot(phatLoot);
            if (lootTable == null) {
                throw new ParseException("Invalid loot table: " + phatLoot);
            }

            PhatLootsAPI.link(itemStack, lootTable);

            int slot = Integer.parseInt(this.getOrDefault(SLOT_KEY, "-1"));
            if (slot == -1) {
                arenaPlayer.getPlayer().getInventory().addItem(itemStack);
            } else {
                arenaPlayer.getPlayer().getInventory().setItem(slot, itemStack);
            }
        } catch (ParseException e) {
            ParseException.handle(e
                    .context("Action", "GiveLootBagAction")
                    .context("Arena", arenaPlayer.getArena().getName())
                    .context("Provided item", this.get(item))
                    .context("Provided loot table", this.get(phatLoot))
                    .cause(ParseException.Cause.INVALID_VALUE)
                    .userError()
            );
        }
    }
}
