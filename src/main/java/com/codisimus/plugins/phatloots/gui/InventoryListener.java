package com.codisimus.plugins.phatloots.gui;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.loot.Item;
import com.codisimus.plugins.phatloots.loot.Loot;
import com.codisimus.plugins.phatloots.loot.LootCollection;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listens for interactions with PhatLoot info GUIs
 *
 * @author Cody
 */

// TODO: Remove all of the static methods and variables from this class
public class InventoryListener implements Listener {
    private static final int NAVIGATE_AND_MOVE = 0;
    private static final int MODIFY_PROBABILITY_AND_TOGGLE = 1;
    private static final int MODIFY_AMOUNT = 2;
    private final static int SIZE = 54;
    final static int TOOL_SLOT = SIZE - 9;
    private static final HashMap<UUID, Boolean> switchingPages = new HashMap<>(); //Player -> Going deeper (true) or back (false)
    private static final HashMap<UUID, PhatLoot> infoViewers = new HashMap<>(); //Player -> PhatLoot they are viewing
    private static final HashMap<UUID, Stack<InventoryView>> pageStacks = new HashMap<>(); //Player -> Page history
    private static final HashMap<UUID, Loot> holding = new HashMap<>(); //Player -> Loot on cursor
    private static final HashMap<Integer, Button> buttons = new HashMap<>();

    static {
        if (NAVIGATE_AND_MOVE != Tool.getToolByName("NAVIGATE_AND_MOVE").getID()
                || MODIFY_PROBABILITY_AND_TOGGLE != Tool.getToolByName("MODIFY_PROBABILITY_AND_TOGGLE").getID()
                || MODIFY_AMOUNT != Tool.getToolByName("MODIFY_AMOUNT").getID()) {
            PhatLoots.logger.severe("Tool IDs do not match their expected values, Please notify Codisimus!");
        }
    }

    /**
     * Registers a Button to be added to GUI pages
     *
     * @param button The Button to register
     */
    public static void registerButton(Button button) {
        buttons.put(button.getSlot(), button);
    }

    /**
     * Returns the List of loot for the current Inventory
     *
     * @param phatLoot The PhatLoot that contains the Loot
     * @param invName The name of the current Inventory
     * @return The List of loot for the PhatLoot/LootCollection
     */
    private static List<Loot> getLootList(PhatLoot phatLoot, String invName) {
        LootCollection coll = null;
        if (invName.endsWith(" (Collection)")) {
            coll = phatLoot.findCollection(invName.substring(0, invName.length() - 13));
        }
        return coll == null ? phatLoot.lootList : coll.getLootList();
    }

    /* LISTENERS **/

    /**
     * Processes Players clicking within the PhatLoot GUI
     *
     * @param event The InventoryClickEvent which occurred
     */
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInvClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) {
            return;
        }
        //Return if the Player is not viewing a PhatLoot's info
        Player player = (Player) human;
        UUID playerUUID = player.getUniqueId();
        if (!infoViewers.containsKey(playerUUID)) {
            return;
        } else { //Fixes glitch of not detecting inventory close
            if (!event.getView().getTitle().contains("Loot")
                    && !event.getView().getTitle().contains("Collection")) {
                infoViewers.remove(playerUUID).save(); //Save the PhatLoot in case it has been modified
                pageStacks.get(playerUUID).empty();
                pageStacks.remove(playerUUID);
                return;
            }
        }

        // If the player is clicking in the PhatLoot menu and not their inventory
        boolean inPhatLoot = false;
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory()))
            inPhatLoot = true;

        //Don't allow any inventory clicking
        event.setCancelled(true);

        //Store popularly accessed variables
        PhatLoot phatLoot = infoViewers.get(playerUUID);
        Inventory inv = event.getInventory();
        Tool tool = Tool.getTool(inv.getItem(TOOL_SLOT));
        ItemStack stack = event.getCurrentItem();
        List<Loot> lootList = getLootList(phatLoot, event.getView().getTitle());
        int slot = event.getRawSlot();
        Loot loot = slot >= 0 && lootList.size() > slot ? lootList.get(slot) : null;

        //Check if the Player is holding a Loot
        if (holding.containsKey(playerUUID)) {
            switch (event.getClick()) {
            case LEFT: //Allow
                if (loot == null) {
                    if (slot == lootList.size()) { //Put down Loot
                        lootList.add(holding.remove(playerUUID));
                        event.setCurrentItem(event.getCursor());
                    } else if (slot > lootList.size() && slot < TOOL_SLOT) { //Support adding the Loot to any slot
                        lootList.add(holding.remove(playerUUID));
                        inv.setItem(lootList.size() - 1, event.getCursor());
                        player.updateInventory();
                    } else if (slot == -999) { //Remove Loot
                        holding.remove(playerUUID);
                    } else {
                        break;
                    }
                    event.getView().setCursor(null);
                    return;
                }
                break;
            case RIGHT: //Go back a page
                if (slot < 45) { //Minecraft crash avoidance
                    up(player);
                }
                return;
            default: //Deny all other actions
                return;
            }
        }

        // Set to air since in future versions this returns an NPE
        if (stack == null)
            stack = new ItemStack(Material.AIR);

        /* Switch Tools **/
        if (slot == -999 || (inPhatLoot && slot == TOOL_SLOT)) {
            switch (event.getClick()) {
            case LEFT: //Previous Tool
                //Deny switching toos while holding Loot
                if (!holding.containsKey(playerUUID)) {
                    inv.setItem(TOOL_SLOT, tool.prevTool().getItem());
                    player.updateInventory();
                }
                break;
            case RIGHT: //Next Tool
                inv.setItem(TOOL_SLOT, tool.nextTool().getItem());
                player.updateInventory();
                break;
            default: //Do nothing
                break;
            }
            return;
        }

        /* Go back to a previous View **/
        if (inPhatLoot && stack.getType() == Material.LADDER) {
            ItemMeta details = stack.getItemMeta();
            if (details.hasDisplayName()) {
                String name = stack.getItemMeta().getDisplayName();
                switch (name) {
                case "§2Up to...":
                    //Back one view
                    up(player);
                    return;
                case "§2Back to top...":
                    //Back to first view
                    viewPhatLoot(player, phatLoot);
                    return;
                default:
                    break;
                }
            }
        }

        if (inPhatLoot && stack.getType() == Material.ACACIA_DOOR) {
            ItemMeta details = stack.getItemMeta();
            if (details.hasDisplayName() && details.getDisplayName().contains("Loot Conditions")) {
                if (event.getSlot() == SIZE - 4) {
                    InventoryConditionListener.viewConditionMenu(player, phatLoot);
                    return;
                }
            }
        }

        /* Check if a Button was Clicked **/
        if (buttons.containsKey(slot)) {
            if (inPhatLoot && buttons.get(slot).onClick(event.getClick(), inv, phatLoot, lootList)) {
                refreshPage(player, inv, lootList);
            }
            return;
        }

        /* No Loot was Clicked **/
        if (loot == null) {// loot is an Item in the top inventory of the GUI, part of LootList
            if (tool.getID() == NAVIGATE_AND_MOVE) {
                switch (event.getClick()) {
                case LEFT: //Add an Item as Loot
                    if (slot > SIZE) {
                        ItemStack item = stack.clone();
                        lootList.add(new Item(item, 0));
                        refreshPage(player, inv, lootList);
                    }
                    break;
                case RIGHT: //Go back a page
                    up(player);
                    break;
                }
                return;
            }

            int amount = 0;
            boolean both = false;
            if (tool.getID() == MODIFY_AMOUNT) {
                switch (event.getClick()) {
                case LEFT: //+1 amount
                    amount = 1;
                    both = true;
                    break;
                case DOUBLE_CLICK: //+9 amount
                    amount = 9;
                    both = true;
                    break;
                case RIGHT: //-1 amount
                    amount = -1;
                    both = true;
                    break;
                case SHIFT_LEFT: //+1 upper amount
                    amount = 1;
                    both = false;
                    break;
                case SHIFT_RIGHT: //-1 upper amount
                    amount = -1;
                    both = false;
                    break;
                default:
                    return;
                }
            }

            if (event.getView().getTitle().endsWith("Loot Tables")) { //Default View
                ItemStack infoStack;
                ItemMeta info = Bukkit.getItemFactory().getItemMeta(Material.STONE); //Block type doesn't really matter
                List<String> details = new ArrayList<>();

                switch (slot) {
                case SIZE - 3: //Toggle Break and Respawn
                    if (tool.getID() == MODIFY_AMOUNT) {
                        return;
                    }
                    phatLoot.breakAndRespawn = !phatLoot.breakAndRespawn;

                    //Show the break and respawn status
                    infoStack = new ItemStack(phatLoot.breakAndRespawn ? Material.SPAWNER : Material.CHEST);
                    info.setDisplayName("§4Break and Respawn: §6" + phatLoot.breakAndRespawn);
                    if (phatLoot.breakAndRespawn) {
                        details.add("§6This chest will break after it is looted");
                        details.add("§6and respawn once it may be looted again.");
                    } else {
                        details.add("§6This chest will always be present");
                        details.add("§6even after it is looted.");
                    }
                    break;

                case SIZE - 2: //Toggle AutoLoot
                    if (tool.getID() == MODIFY_AMOUNT) {
                        return;
                    }
                    phatLoot.autoLoot = !phatLoot.autoLoot;

                    //Show the autoloot status
                    infoStack = new ItemStack(phatLoot.autoLoot ? Material.REDSTONE_TORCH : Material.LEVER);
                    info.setDisplayName("§4AutoLoot: §6" + phatLoot.autoLoot);
                    break;

                case SIZE - 1: //Toggle Global/Round or Modify Reset Time
                    if (tool.getID() == MODIFY_AMOUNT) {
                        if (amount == 0) {
                            phatLoot.days = 0;
                            phatLoot.hours = 0;
                            phatLoot.minutes = 0;
                            phatLoot.seconds = 0;
                        } else if (both) {
                            phatLoot.hours += amount;
                        } else {
                            phatLoot.minutes += amount;
                        }
                    } else if (event.getClick() == ClickType.LEFT) {
                        phatLoot.global = !phatLoot.global;
                    } else {
                        phatLoot.round = !phatLoot.round;
                    }

                    //Show the Reset Time
                    infoStack = new ItemStack(Material.CLOCK);
                    info.setDisplayName("§2Reset Time");
                    details.add("§4Days: §6" + phatLoot.days);
                    details.add("§4Hours: §6" + phatLoot.hours);
                    details.add("§4Minutes: §6" + phatLoot.minutes);
                    details.add("§4Seconds: §6" + phatLoot.seconds);
                    details.add("§4Reset Type: §6" + (phatLoot.global ? "Global" : "Individual"));
                    if (phatLoot.round) {
                        details.add("§6Time is rounded down");
                    }
                    break;

                default:
                    return;
                }

                info.setLore(details);
                infoStack.setItemMeta(info);
                event.setCurrentItem(infoStack);
            }
            return;
        }

        /* Action determined based on the Tool **/
        switch (tool.getID()) {
        case NAVIGATE_AND_MOVE:
            switch (event.getClick()) {
            case LEFT: // Remove Loot or Enter a Collection
                if (loot instanceof LootCollection) { //Clicked a LootCollection
                    if (holding.containsKey(playerUUID)) { //Place Loot in Collection
                        Loot l = holding.remove(playerUUID);
                        ((LootCollection) loot).addLoot(l);
                        event.getView().setCursor(null);
                    } else { //Enter LootCollection
                        viewCollection(player, ((LootCollection) loot).name);
                    }
                    return;
                }

                // We've entered the collection or returned by now
                //Remove Loot
                lootList.remove(slot);
                refreshPage(player, inv, lootList); //Shifts remaining loot down
                break;
            case RIGHT: //Go back a page
                up(player);
                break;
            case SHIFT_LEFT: //Move Loot left or Pick up a Collection
                if (loot instanceof LootCollection) { //Pick up the Collection
                    if (holding.containsKey(playerUUID)) { //Swap Loot
                        Loot l = holding.remove(playerUUID);
                        holding.put(playerUUID, lootList.get(slot));
                        lootList.set(slot, l);
                        event.setCurrentItem(event.getCursor()); //Put down Loot
                        event.getView().setCursor(stack); //Pick up new Loot
                    } else { //Pick up Loot
                        holding.put(playerUUID, lootList.remove(slot));
                        event.getView().setCursor(stack);
                        refreshPage(player, inv, lootList); //Shifts remaining loot down
                    }
                } else if (slot > 0) { //Move Loot Left
                    lootList.set(slot, lootList.get(slot - 1));
                    lootList.set(slot - 1, loot);
                    refreshPage(player, inv, lootList);
                }
                break;
            case SHIFT_RIGHT: //Move Loot right
                if (slot < lootList.size() - 1) {
                    lootList.set(slot, lootList.get(slot + 1));
                    lootList.set(slot + 1, loot);
                    refreshPage(player, inv, lootList);
                }
                break;
            }
            break;

        case MODIFY_PROBABILITY_AND_TOGGLE:
            switch (event.getClick()) {
            case DOUBLE_CLICK: //+9%
                loot.setProbability(loot.getProbability() + 8);
                //Fall through
            case LEFT: //+1%
                loot.setProbability(loot.getProbability() + 1);
                while (loot.getProbability() > 100) {
                    loot.setProbability(loot.getProbability() - 100);
                }
                break;
            case RIGHT: //-1%
                loot.setProbability(loot.getProbability() - 1);
                while (loot.getProbability() < 0) {
                    loot.setProbability(loot.getProbability() + 100);
                }
                break;
            default:
                if (loot.onToggle(event.getClick())) {
                    break;
                } else {
                    return;
                }
            }
            event.setCurrentItem(loot.getInfoStack());
            break;

        case MODIFY_AMOUNT:
            int amount;
            boolean both;
            switch (event.getClick()) {
            case LEFT: //+1 amount
                amount = 1;
                both = true;
                break;
            case DOUBLE_CLICK: //+9 amount
                amount = 9;
                both = true;
                break;
            case RIGHT: //-1 amount
                amount = -1;
                both = true;
                break;
            case SHIFT_LEFT: //+1 upper amount
                amount = 1;
                both = false;
                break;
            case SHIFT_RIGHT: //-1 upper amount
                amount = -1;
                both = false;
                break;
            default:
                return;
            }

            if (loot.modifyAmount(amount, both)) {
                event.setCurrentItem(loot.getInfoStack());
            }
            break;

        default:
            if (loot.onToolClick(tool, event.getClick())) {
                refreshPage(player, inv, lootList);
            }
            break;
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player)) {
            return;
        }
        //Return if the Player is not viewing a PhatLoot's info
        Player player = (Player) human;
        UUID playerUUID = player.getUniqueId();
        if (!infoViewers.containsKey(playerUUID)) {
            return;
        }

        if (switchingPages.containsKey(playerUUID)) { //Switching pages
            if (switchingPages.get(playerUUID)) { //Going deeper
                pageStacks.get(playerUUID).add(event.getView());
            }
            //The Player has finished switching pages
            switchingPages.remove(playerUUID);
        } else { //Closing info view
            infoViewers.remove(playerUUID).save(); //Save the PhatLoot in case it has been modified
            pageStacks.get(playerUUID).empty();
            pageStacks.remove(playerUUID);
        }

        //Don't drop items
        if (holding.containsKey(playerUUID)) {
            holding.remove(playerUUID);
            event.getView().setCursor(null);
        }
    }

    /* END LISTENERS **/
    /* INVENTORY VIEWS **/

    /**
     * Opens an Inventory GUI for the given PhatLoot to the given Player
     *
     * @param player The given Player
     * @param phatLoot The given PhatLoot
     */
    public static void viewPhatLoot(Player player, PhatLoot phatLoot) {
        //Get the Inventory title
        String invName;
        if (phatLoot.name.length() <= 20) {
            invName = phatLoot.name + " 战利品表";
        } else if (phatLoot.name.length() <= 32) {
            invName = phatLoot.name;
        } else {
            invName = phatLoot.name.substring(0, 32);
        }

        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, SIZE, invName);

        int index = SIZE;
        ItemStack infoStack;
        ItemMeta info;

        //Display the Reset Time
        infoStack = new ItemStack(Material.CLOCK);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2重置时间");
        List<String> details = new ArrayList<>();
        details.add("§4天数: §6" + phatLoot.days);
        details.add("§4小时: §6" + phatLoot.hours);
        details.add("§4分钟: §6" + phatLoot.minutes);
        details.add("§4秒钟: §6" + phatLoot.seconds);
        details.add("§4重置类型: §6" + (phatLoot.global ? "全局" : "独立"));
        if (phatLoot.round) {
            details.add("§6时间向下取整");
        }
        info.setLore(details);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Display the autoloot status
        infoStack = new ItemStack(phatLoot.autoLoot ? Material.REDSTONE_TORCH : Material.LEVER);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§4自动拾取: §6" + phatLoot.autoLoot);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Display the break and respawn status
        infoStack = new ItemStack(phatLoot.breakAndRespawn ? Material.SPAWNER : Material.CHEST);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§4破坏后重生: §6" + phatLoot.breakAndRespawn);
        details = new ArrayList<>();
        if (phatLoot.breakAndRespawn) {
            details.add("§6这个箱子在被拾取后会破坏");
            details.add("§6并在可以再次拾取时重生.");
        } else {
            details.add("§6这个箱子将始终存在");
            details.add("§6即使在被拾取后.");
        }
        info.setLore(details);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        infoStack = new ItemStack(Material.ACACIA_DOOR);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§e战利品条件");
        details = new ArrayList<>();
        details.add("§6战利品条件是可配置的选项");
        details.add("§只有当满足条件时");
        details.add("§6才能拾取战利品表中的物品");
        info.setLore(details);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Store the current view in the Player's stack
        pageStacks.put(player.getUniqueId(), new Stack<>());

        infoViewers.put(player.getUniqueId(), phatLoot);
        switchView(player, inv, invName);
    }

    /**
     * Opens an Inventory GUI for the specified LootCollection to the given Player
     *
     * @param player The given Player
     * @param name The name of the LootCollection to open
     */
    private static void viewCollection(Player player, String name) {
        //Store the current view in the Player's stack
        pageStacks.get(player.getUniqueId()).add(player.getOpenInventory());

        //Get the Inventory title
        String invName;
        if (name.length() < 20) {
            invName = name + " (子战利品表)";
        } else if (name.length() <= 32) {
            invName = name;
        } else {
            invName = name.substring(0, 32);
        }

        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, SIZE, invName);

        //Create the Back to top button
        ItemStack item = new ItemStack(Material.LADDER);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2返回顶层...");
        item.setItemMeta(info);
        inv.setItem(SIZE - 2, item);

        //Create the Up button
        item = new ItemStack(Material.LADDER);
        info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2返回上一层...");
        List<String> details = new ArrayList<>();
        details.add("§6" + pageStacks.get(player.getUniqueId()).peek().getTitle());
        info.setLore(details);
        item.setItemMeta(info);
        inv.setItem(SIZE - 1, item);

        switchView(player, inv, invName);
    }

    /**
     * Opens the top Inventory in the given Player's Stack
     *
     * @param player The given Player
     */
    private static void up(Player player) {
        if (!pageStacks.isEmpty()) {
            Stack<InventoryView> history = pageStacks.get(player.getUniqueId());
            if (!history.empty()) {
                InventoryView view = history.pop();
                switchView(player, view.getTopInventory(), view.getTitle());
            }
        }
    }

    /**
     * Switches the Player to viewing the given Inventory
     *
     * @param player The Player to open the Inventory
     * @param inv The Inventory to open
     * @param invName The name of the Inventory to open
     */
    private static void switchView(final Player player, final Inventory inv, final String invName) {
        UUID playerUUID = player.getUniqueId();
        switchingPages.put(playerUUID, false);

        //Get the Loot that the Player is holding
        final ItemStack hand = player.getItemOnCursor();
        Loot loot = holding.get(playerUUID);

        //Close the old InventoryView **after here it wasn't working at all**
//        player.closeInventory();

        //Re-add the Loot if there was any (it is removed onInventoryClose)
        if (loot != null) {
            holding.put(playerUUID, loot);
        }

        //Display the default Tool
        inv.setItem(TOOL_SLOT, Tool.getToolByID(0).getItem());

        //Add Buttons to view
        for (int i = 1; i <= buttons.size(); i++) {
            int slot = TOOL_SLOT + i;
            inv.setItem(slot, buttons.get(slot).getItem());
        }

        //Populate the inventory
        refreshPage(player, inv, getLootList(infoViewers.get(player.getUniqueId()), invName));

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        Bukkit.getScheduler().runTaskLater(PhatLoots.plugin, () -> {
            //Open the Inventory and place the ItemStack on the cursor
            player.openInventory(inv).setCursor(hand);
        }, 2);
    }

    /**
     * Refreshes each InfoView within the Inventory
     *
     * @param player The Player viewing the Inventory
     * @param inv The Inventory being viewed
     * @param lootList The list of loot to display
     */
    private static void refreshPage(Player player, Inventory inv, List<Loot> lootList) {
        //Populate the inventory with the Loot items
        int index = 0;
        for (Loot loot : lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= TOOL_SLOT) {
                player.sendMessage("§4无法显示所有物品.");
                break;
            }
        }
        //Clear the rest of the Loot slots
        while (index < TOOL_SLOT) {
            inv.clear(index);
            index++;
        }
        player.updateInventory();
    }

    /* END INVENTORY VIEWS **/
}
