package com.codisimus.plugins.phatloots.hook;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.hook.placeholder.PlaceholderManager;
import com.codisimus.plugins.phatloots.hook.worldguard.WorldGuardManager;

/**
 * Manager for managing certain plugin hooks
 *
 * @author Redned
 */
public class PluginHookManager {

    private PlaceholderManager placeholderManager;
    private WorldGuardManager worldGuardManager;

    public PluginHookManager(PhatLoots plugin) {
        placeholderManager = new PlaceholderManager(plugin);
        worldGuardManager = new WorldGuardManager(plugin);
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }
}
