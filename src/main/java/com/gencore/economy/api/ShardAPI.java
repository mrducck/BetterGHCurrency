package com.gencore.economy.api;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.database.DatabaseManager;

import java.util.UUID;

/**
 * API for managing player Shards
 *
 * Usage Example:
 *
 * ShardAPI shardAPI = GenCoreEconomy.getInstance().getShardAPI();
 *
 * // Add shards
 * shardAPI.addShards(player.getUniqueId(), 250L);
 *
 * // Remove shards
 * shardAPI.removeShards(player.getUniqueId(), 50L);
 *
 * // Get balance
 * long balance = shardAPI.getShards(player.getUniqueId());
 */
public class ShardAPI {

    private final GenCoreEconomy plugin;
    private final DatabaseManager db;

    public ShardAPI(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    /**
     * Get a player's shard balance
     * @param uuid Player's UUID
     * @return Current shard balance
     */
    public long getShards(UUID uuid) {
        return db.getShards(uuid);
    }

    /**
     * Set a player's shard balance
     * @param uuid Player's UUID
     * @param amount Amount to set
     */
    public void setShards(UUID uuid, long amount) {
        db.setShards(uuid, Math.max(0, amount));
    }

    /**
     * Add shards to a player's balance
     * @param uuid Player's UUID
     * @param amount Amount to add
     */
    public void addShards(UUID uuid, long amount) {
        long current = getShards(uuid);
        setShards(uuid, current + amount);
    }

    /**
     * Remove shards from a player's balance
     * @param uuid Player's UUID
     * @param amount Amount to remove
     * @return true if successful, false if insufficient shards
     */
    public boolean removeShards(UUID uuid, long amount) {
        long current = getShards(uuid);
        if (current >= amount) {
            setShards(uuid, current - amount);
            return true;
        }
        return false;
    }

    /**
     * Check if player has at least the specified amount
     * @param uuid Player's UUID
     * @param amount Amount to check
     * @return true if player has enough shards
     */
    public boolean hasShards(UUID uuid, long amount) {
        return getShards(uuid) >= amount;
    }

    /**
     * Reset a player's shards to 0
     * @param uuid Player's UUID
     */
    public void resetShards(UUID uuid) {
        setShards(uuid, 0);
    }

    /**
     * Get formatted shard string (e.g., 1,000 Shards)
     * @param uuid Player's UUID
     * @return Formatted shard string
     */
    public String getFormattedShards(UUID uuid) {
        return formatShards(getShards(uuid));
    }

    /**
     * Format a shard amount
     * @param amount Amount to format
     * @return Formatted string
     */
    public String formatShards(long amount) {
        return String.format("%,d Shards", amount);
    }

    /**
     * Transfer shards between players
     * @param from Sender's UUID
     * @param to Receiver's UUID
     * @param amount Amount to transfer
     * @return true if successful, false if insufficient shards
     */
    public boolean transferShards(UUID from, UUID to, long amount) {
        if (hasShards(from, amount)) {
            removeShards(from, amount);
            addShards(to, amount);
            return true;
        }
        return false;
    }
}