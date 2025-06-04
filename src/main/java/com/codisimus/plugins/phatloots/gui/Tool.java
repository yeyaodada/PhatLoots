package com.codisimus.plugins.phatloots.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A Tool is used for modifying Loot in the GUI
 *
 * @author Codisimus
 */
public class Tool {
    private static final ArrayList<Tool> tools = new ArrayList<>();
    private final int id;
    private final String name;
    private final ItemStack item;

    static {
        ItemStack item = new ItemStack(Material.LEAD);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        List<String> lore = new ArrayList<>();
        meta.setDisplayName("§2战利品管理 (点击切换工具)");
        lore.add("§4左键点击:");
        lore.add("§6 进入子战利品表或添加/移除战利品");
        lore.add("§4右键点击:");
        lore.add("§6 离开子战利品表");
        lore.add("§4Shift + 左键点击:");
        lore.add("§6 Picks up a Collection");
        lore.add("§6 将战利品向左移动");
        lore.add("§4Shift + 右键点击:");
        lore.add("§6 将战利品向右移动");
        meta.setLore(lore);
        item.setItemMeta(meta);
        new Tool("NAVIGATE_AND_MOVE", item).registerTool();

        item = new ItemStack(Material.NAME_TAG);
        meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        lore.clear();
        meta.setDisplayName("§2概率管理 (点击切换工具)");
        lore.add("§4左键点击:");
        lore.add("§6 概率+1");
        lore.add("§4双击左键:");
        lore.add("§6 概率+10");
        lore.add("§4右键点击:");
        lore.add("§6 概率-1");
        lore.add("§4Shift + 左键点击:");
        lore.add("§6 切换自动附魔状态/FromConsole");
       lore.add("§4Shift + 右键点击:");
        lore.add("§6 切换自动命名状态/TempOP");
        meta.setLore(lore);
        item.setItemMeta(meta);
        new Tool("MODIFY_PROBABILITY_AND_TOGGLE", item).registerTool();

        item = new ItemStack(Material.GOLD_NUGGET);
        meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        lore.clear();
        meta.setDisplayName("§2数量管理 (点击切换工具)");
         lore.add("§4左键点击:");
        lore.add("§6 数量+1");
        lore.add("§4双击左键:");
        lore.add("§6 数量+10");
        lore.add("§4右键点击:");
        lore.add("§6 数量-1");
        lore.add("§4Shift + 左键点击:");
        lore.add("§6 +1 数量 (最大数量)");
        lore.add("§4Shift + 右键点击:");
        lore.add("§6 -1 数量 (最大数量)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        new Tool("MODIFY_AMOUNT", item).registerTool();
    }

    /**
     * Constructs a new Tool
     *
     * @param name The unique name of the tool
     * @param item The ItemStack that represents the Tool
     */
    public Tool(String name, ItemStack item) {
        id = tools.size();
        this.name = name;
        this.item = item;
    }

    /**
     * Returns the id of the Tool
     *
     * @return The id of the Tool
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the name of the Tool
     *
     * @return The name of the Tool
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ItemStack which displays the Tool information
     *
     * @return The Tool's ItemStack
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Returns the previous Tool based on it's id
     *
     * @return The previous Tool based on it's id
     */
    public Tool prevTool() {
        int toolID = id - 1;
        if (toolID < 0) {
            toolID = tools.size() - 1;
        }
        return getToolByID(toolID);
    }

    /**
     * Returns the next Tool based on it's id
     *
     * @return The next Tool based on it's id
     */
    public Tool nextTool() {
        int toolID = id + 1;
        if (toolID >= tools.size()) {
            toolID = 0;
        }
        return getToolByID(toolID);
    }

    /**
     * Adds this Tool to the static list of registered tools
     */
    public void registerTool() {
        tools.add(this);
    }

    /**
     * Returns the Tool with the given id
     *
     * @param id The given Tool id
     * @return The Tool with the given id or null if there is no Tool with that id
     */
    public static Tool getToolByID(int id) {
        return id >= tools.size() ? null : tools.get(id);
    }

    /**
     * Returns the Tool with the given name
     *
     * @param name The given Tool name
     * @return The Tool with the given name or null if there is no Tool with that id
     */
    public static Tool getToolByName(String name) {
        for (Tool tool : tools) {
            if (tool.name.equals(name)) {
                return tool;
            }
        }
        return null;
    }

    /**
     * Returns the Tool which is represented by the given ItemStack
     *
     * @param item The given ItemStack
     * @return The Tool that shares the same Material as the ItemStack
     */
    public static Tool getTool(ItemStack item) {
        Material mat = item.getType();
        for (Tool tool : tools) {
            if (tool.item.getType() == mat) {
                return tool;
            }
        }
        return null;
    }
}
