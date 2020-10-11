/* This file is part of SafePizzaTrade.

    Copyright (C) 2020 piz2a

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

*/

package io.github.piz2a.safetrade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class TradeGUI {

    private final SafeTrade plugin;
    public final Player player1, player2;
    public final String name1, name2;
    public Inventory inv;
    public boolean accepting;
    public int availableSlot;
    public final int row;
    public final int BUTTON_ACCEPT, BUTTON_DECLINE, ITEM_ACCEPT, ITEM_OPPONENT_ACCEPT;

    public TradeGUI(SafeTrade plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1 = player1;
        this.player2 = player2;
        name1 = player1.getName();
        name2 = player2.getName();

        accepting = false;

        row = plugin.config.getInt("guiSlotRow");

        availableSlot = 4 * row;
        BUTTON_ACCEPT = 9 * (row + 1);
        BUTTON_DECLINE = BUTTON_ACCEPT + 1;
        ITEM_ACCEPT = 9 * (row + 1) + 3;
        ITEM_OPPONENT_ACCEPT = 9 * (row + 2) - 4;
    }

    public void open() {
        // Create Inventory
        String invName = String.format(plugin.config.getString("guiTitle"), name1, name2);
        inv = Bukkit.createInventory(null, 9 * (row + 2), invName);

        // Place walls
        ItemStack wall = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta wallMeta = wall.getItemMeta();
        wallMeta.setDisplayName(" ");
        wall.setItemMeta(wallMeta);

        // Place buttons and items
        ItemStack acceptButton = new ItemStack(Material.STAINED_CLAY, 1, (short) 13);
        ItemMeta acceptButtonMeta = acceptButton.getItemMeta();
        acceptButtonMeta.setDisplayName(ChatColor.GREEN + plugin.config.getString("acceptButtonName"));
        acceptButton.setItemMeta(acceptButtonMeta);
        inv.setItem(BUTTON_ACCEPT, acceptButton);
        ItemStack declineButton = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
        ItemMeta declineButtonMeta = acceptButton.getItemMeta();
        declineButtonMeta.setDisplayName(ChatColor.RED + plugin.config.getString("declineButtonName"));
        declineButton.setItemMeta(declineButtonMeta);
        inv.setItem(BUTTON_DECLINE, declineButton);
        ItemStack acceptItem = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
        ItemMeta acceptItemMeta = acceptButton.getItemMeta();
        acceptItemMeta.setDisplayName(" ");
        acceptItem.setItemMeta(acceptItemMeta);
        inv.setItem(ITEM_ACCEPT, acceptItem);
        inv.setItem(ITEM_OPPONENT_ACCEPT, acceptItem);

        for (int i = row * 9; i < 9 * (row + 1); i++)
            inv.setItem(i, wall);
        for (int i = 0; i < (row + 2); i++)
            inv.setItem(9 * i + 4, wall);

        player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_TRADING, 1, 10);
        player1.openInventory(inv);
    }

    public void close(boolean wasCompleted) {
        // Log
        plugin.getLogger().info(String.format(plugin.config.getString("tradeExitLog"), name1, name2));

        // Give item
        giveItems(player1, wasCompleted);
        giveItems(player2, wasCompleted);

        // Message & Remove from plugin.tradingPlayers
        String message = String.format(
                (wasCompleted ? ChatColor.GREEN : ChatColor.RED) +
                plugin.config.getString(wasCompleted ? "tradeCompletedMessage" : "tradeCanceledMessage"),
                name1, name2
        );
        plugin.tradingPlayers.remove(name1);
        plugin.tradeGuis.remove(name1);
        player1.sendMessage(message);
        plugin.tradingPlayers.remove(name2);
        plugin.tradeGuis.remove(name2);
        player2.sendMessage(message);
    }

    public void putItem(int slot) {
        if (availableSlot != 0) {
            InventoryView view = player1.getOpenInventory();
            ItemStack item = view.getItem(slot);
            // Remove
            view.setItem(slot, null);

            // Put
            int newRelativeSlot = 4 * row - availableSlot--;
            int newRow = newRelativeSlot / 4, newColumn = newRelativeSlot % 4;
            int newSlot = newRow * 9 + newColumn;
            view.setItem(newSlot, item);
            player2.getOpenInventory().setItem(newRow * 9 + newColumn + 5, item);

            player1.updateInventory();
            player2.updateInventory();
        } else {
            player1.sendMessage(ChatColor.RED + plugin.config.getString("cantPutItem"));
        }
    }

    public void pickUpItem(int slot) {
        InventoryView view = player1.getOpenInventory();
        ItemStack item = view.getItem(slot);

        // Remove
        view.setItem(slot, null);
        player2.getOpenInventory().setItem(slot + 5, null);
        availableSlot++;

        int firstEmpty = getFirstEmpty(player1);
        if (firstEmpty != -1) {
            view.getBottomInventory().setItem(firstEmpty, item);
        } else {
            player1.getWorld().dropItem(player1.getLocation(), item);
        }

        player1.updateInventory();
        player2.updateInventory();
    }

    private void giveItems(Player player, boolean wasCompleted) {
        InventoryView view1 = player.getOpenInventory();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < 4; j++) {
                int slot = i * 9 + (wasCompleted ? j + 5 : j);
                ItemStack item = view1.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    int newSlot = getFirstEmpty(player1);
                    view1.setItem(slot, null);
                    if (newSlot != -1) {
                        view1.getBottomInventory().setItem(newSlot, item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
            }
        }
    }

    private int getFirstEmpty(Player player) {
        Inventory inv = player.getOpenInventory().getBottomInventory();
        for (int i = 0; i < 4 * 9; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }

}
