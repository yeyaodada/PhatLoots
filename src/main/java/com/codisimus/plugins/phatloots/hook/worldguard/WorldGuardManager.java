package com.codisimus.plugins.phatloots.hook.worldguard;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.block.Block;
import org.bukkit.plugin.PluginManager;

public class WorldGuardManager {

    private WorldGuard worldGuardPlugin;

    public WorldGuardManager(PhatLoots plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (pluginManager.getPlugin("WorldGuard") != null) {
            worldGuardPlugin = WorldGuard.getInstance();
        }
    }

    public WorldGuard getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    public boolean isRegionLootableIfOwnerOrMembers(Block block) {
        if (worldGuardPlugin != null && PhatLootsConfig.checkIfRegionHasOwnerOrMember) {
            World wgWorld = BukkitAdapter.adapt(block.getLocation().getWorld());
            ApplicableRegionSet applicableRegionSet = worldGuardPlugin.getPlatform().getRegionContainer().get(wgWorld).getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));

            for (ProtectedRegion protectedRegion : applicableRegionSet.getRegions()) {
                if ((PhatLootsConfig.cancelIfRegionHasPlayerOwner && protectedRegion.getOwners().getPlayerDomain().size() > 0)) {
                    return false;
                }
                if (PhatLootsConfig.cancelIfRegionHasPlayerMember && protectedRegion.getMembers().getPlayerDomain().size() > 0) {
                    return false;
                }
                if (PhatLootsConfig.cancelIfRegionHasGroupOwner && protectedRegion.getOwners().getGroupDomain().size() > 0) {
                    return false;
                }
                if (PhatLootsConfig.cancelIfRegionHasGroupMember && protectedRegion.getMembers().getGroupDomain().size() > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
