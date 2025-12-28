package com.gencore.economy.hooks;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.util.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for GenCoreEconomy
 *
 * Available Placeholders:
 *
 * MONEY:
 * %gencore_money% - Player's money balance (formatted with $)
 * %gencore_money_raw% - Raw money amount (no formatting)
 * %gencore_money_short% - Abbreviated format (e.g., $1.5M)
 *
 * TOKENS:
 * %gencore_tokens% - Player's token balance (formatted with commas)
 * %gencore_tokens_raw% - Raw token amount
 * %gencore_tokens_short% - Abbreviated format (e.g., 1.5M)
 *
 * SHARDS:
 * %gencore_shards% - Player's shard balance (formatted with commas)
 * %gencore_shards_raw% - Raw shard amount
 * %gencore_shards_short% - Abbreviated format (e.g., 500K)
 *
 * CREDITS:
 * %gencore_credits% - Player's credit balance (formatted with commas)
 * %gencore_credits_raw% - Raw credit amount
 * %gencore_credits_short% - Abbreviated format (e.g., 2.5K)
 *
 * LEVELS:
 * %gencore_level% - Player's current level
 * %gencore_experience% - Player's experience points
 * %gencore_xp_short% - Abbreviated XP format
 *
 * REBIRTHS:
 * %gencore_rebirths% - Player's rebirth count
 * %gencore_rebirth_required% - Level required for next rebirth
 * %gencore_rebirth_remaining% - Levels until next rebirth
 * %gencore_rebirth_can% - Can the player rebirth? (Yes/No)
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final GenCoreEconomy plugin;

    public PlaceholderAPIHook(GenCoreEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gencore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GenCore";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params.toLowerCase()) {
            // MONEY PLACEHOLDERS
            case "money":
                return plugin.getMoneyAPI().getFormattedMoney(player.getUniqueId());
            case "money_raw":
                return String.valueOf(plugin.getMoneyAPI().getMoney(player.getUniqueId()));
            case "money_short":
                return "$" + NumberFormatter.formatAbbreviated(
                        plugin.getMoneyAPI().getMoney(player.getUniqueId()));

            // TOKEN PLACEHOLDERS
            case "tokens":
                return NumberFormatter.formatNumber(
                        plugin.getTokenAPI().getTokens(player.getUniqueId()));
            case "tokens_raw":
                return String.valueOf(plugin.getTokenAPI().getTokens(player.getUniqueId()));
            case "tokens_short":
                return NumberFormatter.formatAbbreviated(
                        plugin.getTokenAPI().getTokens(player.getUniqueId()));

            // SHARD PLACEHOLDERS
            case "shards":
                return NumberFormatter.formatNumber(
                        plugin.getShardAPI().getShards(player.getUniqueId()));
            case "shards_raw":
                return String.valueOf(plugin.getShardAPI().getShards(player.getUniqueId()));
            case "shards_short":
                return NumberFormatter.formatAbbreviated(
                        plugin.getShardAPI().getShards(player.getUniqueId()));

            // CREDIT PLACEHOLDERS
            case "credits":
                return NumberFormatter.formatNumber(
                        plugin.getCreditAPI().getCredits(player.getUniqueId()));
            case "credits_raw":
                return String.valueOf(plugin.getCreditAPI().getCredits(player.getUniqueId()));
            case "credits_short":
                return NumberFormatter.formatAbbreviated(
                        plugin.getCreditAPI().getCredits(player.getUniqueId()));

            // LEVEL PLACEHOLDERS
            case "level":
                return String.valueOf(plugin.getLevelAPI().getLevel(player.getUniqueId()));
            case "experience":
            case "xp":
                return String.format("%.2f",
                        plugin.getLevelAPI().getExperience(player.getUniqueId()));
            case "xp_short":
            case "experience_short":
                return NumberFormatter.formatAbbreviated(
                        plugin.getLevelAPI().getExperience(player.getUniqueId()));

            // REBIRTH PLACEHOLDERS
            case "rebirths":
            case "rebirth":
                return String.valueOf(plugin.getRebirthAPI().getRebirths(player.getUniqueId()));
            case "rebirth_required":
            case "rebirth_req":
                return String.valueOf(
                        plugin.getRebirthAPI().getRequiredLevel(player.getUniqueId()));
            case "rebirth_remaining":
            case "rebirth_left":
                return String.valueOf(
                        plugin.getRebirthAPI().getLevelsUntilRebirth(player.getUniqueId()));
            case "rebirth_can":
            case "can_rebirth":
                return plugin.getRebirthAPI().canRebirth(player.getUniqueId()) ? "Yes" : "No";

            default:
                return null;
        }
    }
}