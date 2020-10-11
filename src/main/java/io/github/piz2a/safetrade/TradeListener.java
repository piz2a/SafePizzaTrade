package io.github.piz2a.safetrade;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;


public class TradeListener implements Listener {

    private final SafeTrade plugin;

    public TradeListener(SafeTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (plugin.config.getBoolean("enableShiftRightClick")) {
            Player player = event.getPlayer();
            String name = player.getName();
            Entity entity = event.getRightClicked();
            long currentTime = System.currentTimeMillis();
            if (entity instanceof Player && player.isSneaking()) {
                if (!plugin.interactCoolDown.containsKey(name)
                        || plugin.interactCoolDown.get(name) + 300 <= currentTime) {
                    plugin.interactCoolDown.put(name, currentTime);
                    player.performCommand("trade " + entity.getName());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        TradeGUI gui = plugin.tradeGuis.get(player.getName());
        if (plugin.tradingPlayers.containsKey(player.getName())) {
            event.setCancelled(true);

            int slot = event.getRawSlot();
            int row = slot / 9, column = slot % 9;
            InventoryView view = event.getView();
            // player.sendMessage("Slot: " + slot);

            if (row < gui.row && column < 4) {
                ItemStack acceptItem = new ItemStack(view.getItem(gui.ITEM_ACCEPT).getType(), 1, (short) 14);

                view.setItem(gui.ITEM_ACCEPT, acceptItem);
                view.setItem(gui.ITEM_OPPONENT_ACCEPT, acceptItem);
                gui.accepting = false;
                player.updateInventory();

                TradeGUI gui2 = plugin.tradeGuis.get(gui.player2.getName());
                InventoryView view2 = gui.player2.getOpenInventory();
                view2.setItem(gui.ITEM_ACCEPT, acceptItem);
                view2.setItem(gui.ITEM_OPPONENT_ACCEPT, acceptItem);
                gui2.accepting = false;
                gui.player2.updateInventory();

                ItemStack item = view.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    gui.pickUpItem(slot);
                }
            } else if (slot == gui.BUTTON_ACCEPT) {
                ItemStack acceptItem = new ItemStack(view.getItem(gui.ITEM_ACCEPT).getType(), 1, (short) 5);

                view.setItem(gui.ITEM_ACCEPT, acceptItem);
                gui.player2.getOpenInventory().setItem(gui.ITEM_OPPONENT_ACCEPT, acceptItem);
                gui.accepting = true;

                // Sound
                gui.player1.playSound(gui.player1.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);
                gui.player2.playSound(gui.player2.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);

                TradeGUI gui2 = plugin.tradeGuis.get(gui.player2.getName());
                if (gui2.accepting) {
                    gui.close(true);
                    player.closeInventory();
                    gui.player2.closeInventory();

                    // Sound
                    gui.player1.playSound(gui.player1.getLocation(), Sound.BLOCK_NOTE_HARP, 1, 3);
                    gui.player2.playSound(gui.player1.getLocation(), Sound.BLOCK_NOTE_HARP, 1, 3);
                }
            } else if (slot == gui.BUTTON_DECLINE) {
                gui.close(false);
                player.closeInventory();
                gui.player2.closeInventory();
            } else if ((gui.row + 2) * 9 <= slot) {
                ItemStack acceptItem = new ItemStack(view.getItem(gui.ITEM_ACCEPT).getType(), 1, (short) 14);

                view.setItem(gui.ITEM_ACCEPT, acceptItem);
                view.setItem(gui.ITEM_OPPONENT_ACCEPT, acceptItem);
                gui.accepting = false;
                player.updateInventory();

                TradeGUI gui2 = plugin.tradeGuis.get(gui.player2.getName());
                InventoryView view2 = gui.player2.getOpenInventory();
                view2.setItem(gui.ITEM_ACCEPT, acceptItem);
                view2.setItem(gui.ITEM_OPPONENT_ACCEPT, acceptItem);
                gui2.accepting = false;
                gui.player2.updateInventory();

                gui.putItem(slot);
            }
        }
        // Debug
        // player.sendMessage(event.getClickedInventory().getName() + " : " + event.getRawSlot());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        TradeGUI gui = plugin.tradeGuis.get(player.getName());
        if (!plugin.playersWhoseInventoryIsClosedByOpponent.contains(player.getName())) {
            if (plugin.tradingPlayers.containsKey(player.getName())) {
                gui.close(false);
                gui.player2.closeInventory();
                // plugin.tradeGuis.get(gui.player2.getName()).close(false);
                plugin.playersWhoseInventoryIsClosedByOpponent.add(gui.player2.getName());
            }
        } else {
            plugin.playersWhoseInventoryIsClosedByOpponent.remove(player.getName());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (plugin.tradingPlayers.containsKey(player.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Player player = (Player) event.getInitiator().getViewers().get(0);
        if (plugin.tradingPlayers.containsKey(player.getName())) {
            event.setCancelled(true);
        }
    }

}
