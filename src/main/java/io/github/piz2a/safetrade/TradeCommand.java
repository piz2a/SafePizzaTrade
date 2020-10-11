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

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TradeCommand implements CommandExecutor {

    private final SafeTrade plugin;

    public TradeCommand(SafeTrade plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (split.length == 1) {
            if (sender instanceof Player) {
                Player player1 = (Player) sender;
                if (plugin.config.getBoolean("makeCommandOnlyForOP")) {
                    if (!player1.isOp()) {
                        sender.sendMessage(ChatColor.RED + plugin.config.getString("permissionMessage"));
                        return true;
                    }
                }
                if (!plugin.config.getStringList("blacklistedWorlds").contains(player1.getWorld().getName())) {
                    String name1 = player1.getName();

                    // Check player1 not trading
                    if (!plugin.tradingPlayers.containsKey(name1)) {
                        String playerName = split[0];
                        Player player2 = plugin.getServer().getPlayer(playerName);
                        String name2 = player2.getName();

                        if (name1.equals(name2)) {
                            sender.sendMessage(plugin.config.getString("cannotTradeWithYourself"));
                            return true;
                        }

                        Location loc1 = player1.getLocation();
                        Location loc2 = player2.getLocation();

                        double distance = Math.sqrt(
                                Math.pow(loc1.getX() - loc2.getX(), 2)
                                        + Math.pow(loc1.getY() - loc2.getY(), 2)
                                        + Math.pow(loc1.getZ() - loc2.getZ(), 2)
                        );

                        // Check coolDown
                        int coolDown = plugin.config.getInt("tradeCommandCoolDown");
                        long currentTime = System.currentTimeMillis();
                        if (!plugin.tradeCoolDown.containsKey(name1) ||
                                plugin.tradeCoolDown.get(name1) + coolDown * 1000 <= currentTime) {
                            plugin.tradeCoolDown.put(name1, currentTime);

                            // Check distance
                            if (distance < plugin.config.getDouble("maxTradeDistance")) {
                                // If this is accepting the trade
                                if (plugin.tradeRequestingPlayers.containsKey(name2)
                                        && plugin.tradeRequestingPlayers.get(name2).equals(name1)) {
                                    plugin.tradeRequestingPlayers.remove(name2);
                                    plugin.tradingPlayers.put(name1, name2);
                                    plugin.tradingPlayers.put(name2, name1);

                                    TradeGUI gui1 = new TradeGUI(plugin, player1, player2);
                                    TradeGUI gui2 = new TradeGUI(plugin, player2, player1);
                                    plugin.tradeGuis.put(name1, gui1);
                                    plugin.tradeGuis.put(name2, gui2);
                                    gui1.open();
                                    gui2.open();

                                    plugin.getLogger().info(String.format(plugin.config.getString("tradingLog"), name1, name2));
                                }
                                // If this is requesting the trade
                                else {
                                    player1.sendMessage(
                                            ChatColor.GREEN + String.format(plugin.config.getString("giveRequestMessage"), name2)
                                    );

                                    TextComponent requestMessage = new TextComponent(
                                            ChatColor.GREEN + String.format(plugin.config.getString("requestMessage"), name1)
                                    );
                                    requestMessage.setHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new ComponentBuilder(plugin.config.getString("hoverText")).create())
                                    );
                                    requestMessage.setClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade " + name1)
                                    );
                                    player2.spigot().sendMessage(requestMessage);
                                    plugin.tradeRequestingPlayers.put(name1, name2);
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ExpireTask(
                                            player1, player2, name1, name2
                                    ), 10 * 20L);
                                }
                            } else {
                                // Too far
                                sender.sendMessage(ChatColor.RED + plugin.config.getString("tooFarMessage"));
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + plugin.config.getString("coolDownMessage"));
                        }
                    } else {
                        // Sending trade request while opening inventory
                        sender.sendMessage(ChatColor.RED + "Impossible Situation");
                        plugin.tradingPlayers.remove(name1);
                    }
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + plugin.config.getString("consoleTradingMessage"));
                return true;
            }
        }
        return false;
    }

    private class ExpireTask implements Runnable {

        Player player1, player2;
        String name1, name2;

        ExpireTask(Player player1, Player player2, String name1, String name2) {
            this.player1 = player1;
            this.player2 = player2;
            this.name1 = name1;
            this.name2 = name2;
        }

        public void run() {
            if (plugin.tradeRequestingPlayers.containsKey(name1)
                    && plugin.tradeRequestingPlayers.get(name1).equals(name2)) {
                plugin.tradeRequestingPlayers.remove(name1);
                player1.sendMessage(
                        ChatColor.RED + String.format(plugin.config.getString("myRequestExpiredMessage"), name2)
                );
                player2.sendMessage(
                        ChatColor.RED + String.format(plugin.config.getString("requestExpiredMessage"), name1)
                );
            }
        }
    }

}
