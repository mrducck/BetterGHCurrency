package com.gencore.economy.api;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.database.DatabaseManager;

import java.util.UUID;

/**
 * API for managing player Rebirths
 *
 * Usage Example:
 * RebirthAPI rebirthAPI = GenCoreEconomy.getInstance().getRebirthAPI();
 *
 * // Check if player can rebirth
 * if (rebirthAPI.canRebirth(player.getUniqueId())) {
 *     rebirthAPI.rebirth(player.getUniqueId());
 * }
 *
 * // Get required level for next rebirth
 * int required = rebirthAPI.getRequiredLevel(player.getUniqueId());
 */
public class RebirthAPI {

    private final GenCoreEconomy plugin;
    private final DatabaseManager db;
    private final LevelAPI levelAPI;

    private static final int BASE_REBIRTH_LEVEL = 50;
    private static final int LEVEL_INCREMENT = 50;

    public RebirthAPI(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        this.levelAPI = plugin.getLevelAPI();
    }

    /**
     * Get player's rebirth count
     * @param uuid Player UUID
     * @return Number of rebirths
     */
    public int getRebirths(UUID uuid) {
        return db.getRebirths(uuid);
    }

    /**
     * Set player's rebirth count
     * @param uuid Player UUID
     * @param amount Rebirth count
     */
    public void setRebirths(UUID uuid, int amount) {
        db.setRebirths(uuid, Math.max(0, amount));
    }

    /**
     * Add rebirths to player
     * @param uuid Player UUID
     * @param amount Amount to add
     */
    public void addRebirths(UUID uuid, int amount) {
        setRebirths(uuid, getRebirths(uuid) + amount);
    }

    /**
     * Reset player's rebirths to 0
     * @param uuid Player UUID
     */
    public void resetRebirths(UUID uuid) {
        setRebirths(uuid, 0);
    }

    /**
     * Get the level required for next rebirth
     * @param uuid Player UUID
     * @return Required level
     */
    public int getRequiredLevel(UUID uuid) {
        int rebirths = getRebirths(uuid);
        return BASE_REBIRTH_LEVEL + (rebirths * LEVEL_INCREMENT);
    }

    /**
     * Check if player can rebirth
     * @param uuid Player UUID
     * @return true if player meets level requirement
     */
    public boolean canRebirth(UUID uuid) {
        int currentLevel = levelAPI.getLevel(uuid);
        int requiredLevel = getRequiredLevel(uuid);
        return currentLevel >= requiredLevel;
    }

    /**
     * Perform rebirth for player
     * @param uuid Player UUID
     * @return true if successful, false if requirements not met
     */
    public boolean rebirth(UUID uuid) {
        if (canRebirth(uuid)) {
            addRebirths(uuid, 1);
            levelAPI.resetLevel(uuid);
            db.setExperience(uuid, 0);
            return true;
        }
        return false;
    }

    /**
     * Get levels until next rebirth
     * @param uuid Player UUID
     * @return Levels remaining
     */
    public int getLevelsUntilRebirth(UUID uuid) {
        int current = levelAPI.getLevel(uuid);
        int required = getRequiredLevel(uuid);
        return Math.max(0, required - current);
    }
}
