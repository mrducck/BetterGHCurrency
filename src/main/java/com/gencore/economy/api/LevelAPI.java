package com.gencore.economy.api;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.database.DatabaseManager;

import java.util.UUID;

/**
 * API for managing player Levels
 * Usage Example:
 * LevelAPI levelAPI = GenCoreEconomy.getInstance().getLevelAPI();
 * levelAPI.addLevels(player.getUniqueId(), 5);
 * int level = levelAPI.getLevel(player.getUniqueId());
 */
public class LevelAPI {

    private final GenCoreEconomy plugin;
    private final DatabaseManager db;

    public LevelAPI(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    public int getLevel(UUID uuid) {
        return db.getLevel(uuid);
    }

    public void setLevel(UUID uuid, int level) {
        db.setLevel(uuid, Math.max(0, level));
    }

    public void addLevels(UUID uuid, int amount) {
        setLevel(uuid, getLevel(uuid) + amount);
    }

    public boolean removeLevels(UUID uuid, int amount) {
        int current = getLevel(uuid);
        if (current >= amount) {
            setLevel(uuid, current - amount);
            return true;
        }
        return false;
    }

    public void resetLevel(UUID uuid) {
        setLevel(uuid, 0);
    }

    public boolean hasLevel(UUID uuid, int level) {
        return getLevel(uuid) >= level;
    }

    /**
     * Add experience to player (can implement custom XP formula)
     * @param uuid Player UUID
     * @param xp XP to add
     */
    public void addExperience(UUID uuid, double xp) {
        double current = db.getExperience(uuid);
        db.setExperience(uuid, current + xp);

        // Check for level up (example: 100 XP per level)
        double totalXP = current + xp;
        int newLevel = (int) (totalXP / 100);
        if (newLevel > getLevel(uuid)) {
            setLevel(uuid, newLevel);
        }
    }

    public double getExperience(UUID uuid) {
        return db.getExperience(uuid);
    }
}