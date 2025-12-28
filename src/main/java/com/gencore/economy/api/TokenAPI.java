package com.gencore.economy.api;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.database.DatabaseManager;

import java.util.UUID;

/**
 * API for managing player Tokens
 *
 * Usage Example:
 *
 * TokenAPI tokenAPI = GenCoreEconomy.getInstance().getTokenAPI();
 *
 * // Add tokens
 * tokenAPI.addTokens(player.getUniqueId(), 500L);
 *
 * // Remove tokens
 * tokenAPI.removeTokens(player.getUniqueId(), 100L);
 *
 * // Get balance
 * long balance = tokenAPI.getTokens(player.getUniqueId());
 *
 * // Check if has tokens
 * if (tokenAPI.hasTokens(player.getUniqueId(), 1000L)) {
 *     // Player has enough
 * }
 */
public class TokenAPI {

    private final GenCoreEconomy plugin;
    private final DatabaseManager db;

    public TokenAPI(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    /**
     * Get a player's token balance
     * @param uuid Player's UUID
     * @return Current token balance
     */
    public long getTokens(UUID uuid) {
        return db.getTokens(uuid);
    }

    /**
     * Set a player's token balance
     * @param uuid Player's UUID
     * @param amount Amount to set
     */
    public void setTokens(UUID uuid, long amount) {
        db.setTokens(uuid, Math.max(0, amount));
    }

    /**
     * Add tokens to a player's balance
     * @param uuid Player's UUID
     * @param amount Amount to add
     */
    public void addTokens(UUID uuid, long amount) {
        long current = getTokens(uuid);
        setTokens(uuid, current + amount);
    }

    /**
     * Remove tokens from a player's balance
     * @param uuid Player's UUID
     * @param amount Amount to remove
     * @return true if successful, false if insufficient tokens
     */
    public boolean removeTokens(UUID uuid, long amount) {
        long current = getTokens(uuid);
        if (current >= amount) {
            setTokens(uuid, current - amount);
            return true;
        }
        return false;
    }

    /**
     * Check if player has at least the specified amount
     * @param uuid Player's UUID
     * @param amount Amount to check
     * @return true if player has enough tokens
     */
    public boolean hasTokens(UUID uuid, long amount) {
        return getTokens(uuid) >= amount;
    }

    /**
     * Reset a player's tokens to 0
     * @param uuid Player's UUID
     */
    public void resetTokens(UUID uuid) {
        setTokens(uuid, 0);
    }

    /**
     * Get formatted token string (e.g., 1,000 Tokens)
     * @param uuid Player's UUID
     * @return Formatted token string
     */
    public String getFormattedTokens(UUID uuid) {
        return formatTokens(getTokens(uuid));
    }

    /**
     * Format a token amount
     * @param amount Amount to format
     * @return Formatted string
     */
    public String formatTokens(long amount) {
        return String.format("%,d Tokens", amount);
    }

    /**
     * Transfer tokens between players
     * @param from Sender's UUID
     * @param to Receiver's UUID
     * @param amount Amount to transfer
     * @return true if successful, false if insufficient tokens
     */
    public boolean transferTokens(UUID from, UUID to, long amount) {
        if (hasTokens(from, amount)) {
            removeTokens(from, amount);
            addTokens(to, amount);
            return true;
        }
        return false;
    }
}