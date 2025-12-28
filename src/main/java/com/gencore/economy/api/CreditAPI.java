package com.gencore.economy.api;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.database.DatabaseManager;

import java.util.UUID;

/**
 * API for managing player Credits
 *
 * Usage Example:
 * CreditAPI creditAPI = GenCoreEconomy.getInstance().getCreditAPI();
 * creditAPI.addCredits(player.getUniqueId(), 100L);
 */
public class CreditAPI {

    private final GenCoreEconomy plugin;
    private final DatabaseManager db;

    public CreditAPI(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    public long getCredits(UUID uuid) {
        return db.getCredits(uuid);
    }

    public void setCredits(UUID uuid, long amount) {
        db.setCredits(uuid, Math.max(0, amount));
    }

    public void addCredits(UUID uuid, long amount) {
        setCredits(uuid, getCredits(uuid) + amount);
    }

    public boolean removeCredits(UUID uuid, long amount) {
        if (getCredits(uuid) >= amount) {
            setCredits(uuid, getCredits(uuid) - amount);
            return true;
        }
        return false;
    }

    public boolean hasCredits(UUID uuid, long amount) {
        return getCredits(uuid) >= amount;
    }

    public void resetCredits(UUID uuid) {
        setCredits(uuid, 0);
    }

    public String getFormattedCredits(UUID uuid) {
        return String.format("%,d Credits", getCredits(uuid));
    }

    public boolean transferCredits(UUID from, UUID to, long amount) {
        if (hasCredits(from, amount)) {
            removeCredits(from, amount);
            addCredits(to, amount);
            return true;
        }
        return false;
    }
}