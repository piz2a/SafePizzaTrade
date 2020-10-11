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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;


public class SafeTrade extends JavaPlugin {

    public HashMap<String, String> tradingPlayers = new HashMap<String, String>();
    public HashMap<String, String> tradeRequestingPlayers = new HashMap<String, String>();
    public HashMap<String, TradeGUI> tradeGuis = new HashMap<String, TradeGUI>();
    public HashMap<String, Long> tradeCoolDown = new HashMap<String, Long>();
    public HashMap<String, Long> interactCoolDown = new HashMap<String, Long>();
    public ArrayList<String> playersWhoseInventoryIsClosedByOpponent = new ArrayList<String>();

    private final TradeListener tradeListener = new TradeListener(this);
    public FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(tradeListener, this);
        getLogger().info("Events Loading Complete.");

        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("trade-reload").setExecutor(new TradeConfigReloadCommand(this));
        getCommand("trade-debug").setExecutor(new DebugCommand(this));
        getLogger().info("Commands Loading Complete.");
    }

    @Override
    public void onDisable() {
        saveConfig();
        // getLogger().info("Exiting");
    }

}
