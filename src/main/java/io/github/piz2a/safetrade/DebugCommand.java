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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class DebugCommand implements CommandExecutor {

    private final SafeTrade plugin;

    public DebugCommand(SafeTrade plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.YELLOW + "tradingPlayers:");
            for (String key : plugin.tradingPlayers.keySet()) {
                sender.sendMessage(String.format("- %s : %s", key, plugin.tradingPlayers.get(key)));
            }
            sender.sendMessage(ChatColor.YELLOW + "tradeRequestingPlayers:");
            for (String key : plugin.tradeRequestingPlayers.keySet()) {
                sender.sendMessage(String.format("- %s : %s", key, plugin.tradeRequestingPlayers.get(key)));
            }
            sender.sendMessage(ChatColor.YELLOW + "tradeGuis:");
            for (String key : plugin.tradeGuis.keySet()) {
                sender.sendMessage(String.format("- %s", key));
            }
        } else {
            sender.sendMessage(ChatColor.RED + plugin.config.getString("permissionMessage"));
        }
        return true;
    }

}
