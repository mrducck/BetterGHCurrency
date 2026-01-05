package com.gencore.economy.listeners;

import com.gencore.economy.GenCoreEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerJoinListener implements Listener {

    private final GenCoreEconomy plugin;

    public PlayerJoinListener(GenCoreEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getDatabaseManager().loadPlayerData(event.getPlayer().getUniqueId());
    }
}
