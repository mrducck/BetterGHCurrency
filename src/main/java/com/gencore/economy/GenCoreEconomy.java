package com.gencore.economy;

import com.gencore.economy.api.*;
import com.gencore.economy.commands.*;
import com.gencore.economy.database.DatabaseManager;
import com.gencore.economy.hooks.PlaceholderAPIHook;
import com.gencore.economy.hooks.VaultHook;
import com.gencore.economy.listeners.PlayerJoinListener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class GenCoreEconomy extends JavaPlugin {

    private static GenCoreEconomy instance;
    private DatabaseManager databaseManager;

    // API instances
    private MoneyAPI moneyAPI;
    private TokenAPI tokenAPI;
    private ShardAPI shardAPI;
    private CreditAPI creditAPI;
    private LevelAPI levelAPI;
    private RebirthAPI rebirthAPI;

    private VaultHook vaultHook;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // Initialize APIs
        moneyAPI = new MoneyAPI(this);
        tokenAPI = new TokenAPI(this);
        shardAPI = new ShardAPI(this);
        creditAPI = new CreditAPI(this);
        levelAPI = new LevelAPI(this);
        rebirthAPI = new RebirthAPI(this);

        // Register APIs as services
        getServer().getServicesManager().register(MoneyAPI.class, moneyAPI, this, ServicePriority.Highest);
        getServer().getServicesManager().register(TokenAPI.class, tokenAPI, this, ServicePriority.Highest);
        getServer().getServicesManager().register(ShardAPI.class, shardAPI, this, ServicePriority.Highest);
        getServer().getServicesManager().register(CreditAPI.class, creditAPI, this, ServicePriority.Highest);
        getServer().getServicesManager().register(LevelAPI.class, levelAPI, this, ServicePriority.Highest);
        getServer().getServicesManager().register(RebirthAPI.class, rebirthAPI, this, ServicePriority.Highest);

        // Register commands
        registerCommands();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Hook into Vault
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(this);
            vaultHook.hook();
            getLogger().info("Hooked into Vault successfully!");
        }

        // Hook into PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI successfully!");
        }

        getLogger().info("GenCoreEconomy has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            getLogger().info("Saving all player data...");
            // Save all cached data asynchronously then close
            databaseManager.saveAllAsync().thenRun(() -> {
                databaseManager.close();
                getLogger().info("All data saved successfully!");
            }).join(); // Wait for completion
        }
        getLogger().info("GenCoreEconomy has been disabled!");
    }

    private void registerCommands() {
        getCommand("cash").setExecutor(new CashCommand(this));
        getCommand("tokens").setExecutor(new TokensCommand(this));
        getCommand("shards").setExecutor(new ShardsCommand(this));
        getCommand("credits").setExecutor(new CreditsCommand(this));
        getCommand("levels").setExecutor(new LevelsCommand(this));
        getCommand("rebirth").setExecutor(new RebirthCommand(this));
    }

    // Getters
    public static GenCoreEconomy getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MoneyAPI getMoneyAPI() {
        return moneyAPI;
    }

    public TokenAPI getTokenAPI() {
        return tokenAPI;
    }

    public ShardAPI getShardAPI() {
        return shardAPI;
    }

    public CreditAPI getCreditAPI() {
        return creditAPI;
    }

    public LevelAPI getLevelAPI() {
        return levelAPI;
    }

    public RebirthAPI getRebirthAPI() {
        return rebirthAPI;
    }
}