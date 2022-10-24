package me.brannstrom.sortingsystem.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HopperListener implements Listener {

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryMove(final InventoryMoveItemEvent event) {

        final ItemStack item = event.getItem();

        // Prevent the filter itself from being moved out of the hopper into another container, or from a container into a hopper.
        if(event.getSource().getType() == InventoryType.HOPPER || event.getDestination().getType() == InventoryType.HOPPER) {
            if(isFilter(item)) {
                event.setCancelled(true);
                return;
            }
        }

        if(event.getSource().getType() == InventoryType.HOPPER) {
            final Inventory destination = event.getDestination();

            // Getting all the filters in the destination inventory. If there are none, we don't need to do anything.
            final List<ItemStack> filters = getFilters(destination);
            if(filters.size() == 0) return;

            // boolean to check if the items should be moved or not based on the filters in the destination inventory.
            boolean allowThrough = true;

            // alreadyFiltered is used to check if the item has already been checked and should be allowed through, making sure the allowThrough boolean is not overwritten to false.
            boolean alreadyFiltered = false;

            for(ItemStack filter : filters) {
                if(alreadyFiltered) break;

                // Getting the type of filter; filter, unfilter of filterexact.
                int type = getFilterType(filter);

                if(type == 1) {
                    if(item.getType() != filter.getType()) {
                        allowThrough = false;
                    }
                    else {
                        alreadyFiltered = true;
                        allowThrough = true;
                    }
                } else if(type == 2) {
                    if(item.getType() == filter.getType()) {
                        allowThrough = false;
                        break;
                    }
                } else if(type == 3) {

                    // Cloning the item going through and the filter and "removing" the display name, so we can compare the item to the filter.
                    ItemStack itemClone = item.clone();
                    ItemMeta itemCloneMeta = itemClone.getItemMeta();
                    itemCloneMeta.setDisplayName("");
                    itemClone.setItemMeta(itemCloneMeta);

                    ItemStack filterClone = filter.clone();
                    ItemMeta filterCloneMeta = filterClone.getItemMeta();
                    filterCloneMeta.setDisplayName("");
                    filterClone.setItemMeta(filterCloneMeta);

                    if(!itemClone.isSimilar(filterClone)) {
                        allowThrough = false;
                    }
                    else {
                        alreadyFiltered = true;
                        allowThrough = true;
                    }
                }
            }
            event.setCancelled(!allowThrough);
        }
    }

    // Prevent any filter item from being put into the first slot of the hopper by click and place or shift-clicking
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getClickedInventory() != null) {
            if(event.getView().getTopInventory().getType().equals(InventoryType.HOPPER)) {
                if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    if(isFilter(event.getCurrentItem())) {
                        if(event.getView().getTopInventory().getContents()[0] == null) {
                            event.setCancelled(true);
                        }
                    }
                }
                else if (event.getAction().equals(InventoryAction.PLACE_ALL)) {
                    if (event.getSlot() == 0) {
                        if (isFilter(event.getCursor())) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    // Prevent any filter item from being dragged into the first slot of the hopper
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if(isFilter(event.getOldCursor())) {
            if(event.getView().getTopInventory().getType() == InventoryType.HOPPER) {
                if(event.getRawSlots().contains(0)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // Prevent the hopper from picking up any filter item that is laying on top of it
    @EventHandler
    public void onInventoryPickupItem(final InventoryPickupItemEvent event) {
        if(event.getInventory().getType() == InventoryType.HOPPER) {
            if(isFilter(event.getItem().getItemStack())) {
                event.setCancelled(true);
            }
        }
    }

    // Check if the item is a filter item based on it's display name
    private boolean isFilter(ItemStack item) {
        if(item.hasItemMeta()) {
            if(item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();
                if(displayName.equalsIgnoreCase("filter") || displayName.equalsIgnoreCase("unfilter") || displayName.equalsIgnoreCase("filterexact")) {
                    return true;
                }
            }
        }
        return false;
    }

    // Get all the filters in the inventory (hopper) and return them in a list
    private List<ItemStack> getFilters(Inventory inventory) {
        List<ItemStack> filters = new ArrayList<>();
        for(ItemStack item : inventory.getContents()) {
            if(item != null) {
                if (item.getType() != Material.AIR) {
                    if (isFilter(item)) {
                        filters.add(item);
                    }
                }
            }
        }
        return filters;
    }

    // Get the type of filter based on the display name and return it as an int (Integer, so it can be null)
    private Integer getFilterType(ItemStack filter) {
        String displayName = filter.getItemMeta().getDisplayName();
        if(displayName.equalsIgnoreCase("filter")) {
            return 1;
        }
        else if(displayName.equalsIgnoreCase("unfilter")) {
            return 2;
        }
        else if(displayName.equalsIgnoreCase("filterexact")) {
            return 3;
        }
        return null;
    }
}