package com.gencore.economy.api;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.database.DatabaseManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * API for managing player Money currency
 *
 * Usage Example:
 *
 * // Get API instance
 * MoneyAPI moneyAPI = GenCoreEconomy.getInstance().getMoneyAPI();
 *
 * // Or via ServiceManager
 * MoneyAPI api = Bukkit.getServicesManager().getRegistration(MoneyAPI.class).getProvider();
 *
 * // Add money to player
 * moneyAPI.addMoney(player.getUniqueId(), 1000.0);
 *
 * // Remove money from player
 * moneyAPI.removeMoney(player.getUniqueId(), 500.0);
 *
 * // Set player's money
 * moneyAPI.setMoney(player.getUniqueId(), 5000.0);
 *
 * // Get player's balance
 * double balance = moneyAPI.getMoney(player.getUniqueId());
 *
 * // Check if player has enough money
 * if (moneyAPI.hasMoney(player.getUniqueId(), 1000.0)) {
 *     // Player has enough money
 * }
 *
 * // Get formatted balance
 * String formatted = moneyAPI.getFormattedMoney(player.getUniqueId());
 */
public class MoneyAPI {

    private final GenCoreEconomy plugin;
    private final DatabaseManager db;

    public MoneyAPI(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    /**
     * Get a player's money balance
     * @param uuid Player's UUID
     * @return Current money balance
     */
    public double getMoney(UUID uuid) {
        return db.getMoney(uuid);
    }

    /**
     * Set a player's money balance
     * @param uuid Player's UUID
     * @param amount Amount to set
     */
    public void setMoney(UUID uuid, double amount) {
        db.setMoney(uuid, Math.max(0, amount));
    }

    /**
     * Add money to a player's balance
     * @param uuid Player's UUID
     * @param amount Amount to add
     */
    public void addMoney(UUID uuid, double amount) {
        double current = getMoney(uuid);
        setMoney(uuid, current + amount);
    }

    /**
     * Remove money from a player's balance
     * @param uuid Player's UUID
     * @param amount Amount to remove
     * @return true if successful, false if insufficient funds
     */
    public boolean removeMoney(UUID uuid, double amount) {
        double current = getMoney(uuid);
        if (current >= amount) {
            setMoney(uuid, current - amount);
            return true;
        }
        return false;
    }

    /**
     * Check if player has at least the specified amount
     * @param uuid Player's UUID
     * @param amount Amount to check
     * @return true if player has enough money
     */
    public boolean hasMoney(UUID uuid, double amount) {
        return getMoney(uuid) >= amount;
    }

    /**
     * Reset a player's money to 0
     * @param uuid Player's UUID
     */
    public void resetMoney(UUID uuid) {
        setMoney(uuid, 0);
    }

    /**
     * Get formatted money string (e.g., $1,000.00)
     * @param uuid Player's UUID
     * @return Formatted money string
     */
    public String getFormattedMoney(UUID uuid) {
        return formatMoney(getMoney(uuid));
    }

    /**
     * Format a money amount
     * @param amount Amount to format
     * @return Formatted string
     */
    public String formatMoney(double amount) {
        return String.format("$%,.2f", amount);
    }

    /**
     * Transfer money between players
     * @param from Sender's UUID
     * @param to Receiver's UUID
     * @param amount Amount to transfer
     * @return true if successful, false if insufficient funds
     */
    public boolean transferMoney(UUID from, UUID to, double amount) {
        if (hasMoney(from, amount)) {
            removeMoney(from, amount);
            addMoney(to, amount);
            return true;
        }
        return false;
    }
}