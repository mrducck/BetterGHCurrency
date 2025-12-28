package com.gencore.economy.database;

import com.gencore.economy.GenCoreEconomy;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async database manager supporting both SQLite and MySQL
 * All operations return CompletableFuture for non-blocking performance
 */
public class DatabaseManager {

    private final GenCoreEconomy plugin;
    private HikariDataSource dataSource;
    private final ExecutorService asyncExecutor;


    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public DatabaseManager(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread thread = new Thread(r, "GenCore-Database-Thread");
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }

    public void initialize() {
        String dbType = plugin.getConfig().getString("database.type", "sqlite");

        HikariConfig config = new HikariConfig();

        if (dbType.equalsIgnoreCase("mysql")) {
            String host = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.database", "gencore");
            String username = plugin.getConfig().getString("database.username", "root");
            String password = plugin.getConfig().getString("database.password", "");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "economy.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);

        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_economy (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "money DOUBLE DEFAULT 0, " +
                "tokens BIGINT DEFAULT 0, " +
                "shards BIGINT DEFAULT 0, " +
                "credits BIGINT DEFAULT 0, " +
                "level INT DEFAULT 0, " +
                "experience DOUBLE DEFAULT 0, " +
                "rebirths INT DEFAULT 0" +
                ")";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }


    public CompletableFuture<PlayerData> loadPlayerDataAsync(UUID uuid) {
        // Check cache first (synchronously for speed)
        if (cache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(cache.get(uuid));
        }

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_economy WHERE uuid = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                PlayerData data;
                if (rs.next()) {
                    data = new PlayerData(
                            uuid,
                            rs.getDouble("money"),
                            rs.getLong("tokens"),
                            rs.getLong("shards"),
                            rs.getLong("credits"),
                            rs.getInt("level"),
                            rs.getDouble("experience"),
                            rs.getInt("rebirths")
                    );
                } else {
                    // Create new player data
                    data = new PlayerData(uuid);
                    createPlayerDataSync(uuid);
                }

                cache.put(uuid, data);
                return data;

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
                return new PlayerData(uuid);
            }
        }, asyncExecutor);
    }


    public PlayerData loadPlayerData(UUID uuid) {
        return loadPlayerDataAsync(uuid).join();
    }

    private void createPlayerDataSync(UUID uuid) {
        String sql = "INSERT INTO player_economy (uuid) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            stmt.execute();

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create player data: " + e.getMessage());
        }
    }


    public CompletableFuture<Void> savePlayerDataAsync(UUID uuid, PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE player_economy SET money = ?, tokens = ?, shards = ?, " +
                    "credits = ?, level = ?, experience = ?, rebirths = ? WHERE uuid = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDouble(1, data.money);
                stmt.setLong(2, data.tokens);
                stmt.setLong(3, data.shards);
                stmt.setLong(4, data.credits);
                stmt.setInt(5, data.level);
                stmt.setDouble(6, data.experience);
                stmt.setInt(7, data.rebirths);
                stmt.setString(8, uuid.toString());

                stmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            }
        }, asyncExecutor);
    }


    public void savePlayerData(UUID uuid, PlayerData data) {
        savePlayerDataAsync(uuid, data).join();
    }

    /**
     * Save all cached data asynchronously (for shutdown)
     * @return CompletableFuture that completes when all saves are done
     */
    public CompletableFuture<Void> saveAllAsync() {
        CompletableFuture<?>[] futures = cache.entrySet().stream()
                .map(entry -> savePlayerDataAsync(entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }



    private <T> T getCurrency(UUID uuid, java.util.function.Function<PlayerData, T> extractor) {
        return extractor.apply(loadPlayerData(uuid));
    }

    /**
     * Generic currency setter with async save
     */
    private void setCurrency(UUID uuid, java.util.function.Consumer<PlayerData> updater) {
        PlayerData data = loadPlayerData(uuid);
        updater.accept(data);
        savePlayerDataAsync(uuid, data); // Async save
    }


    public double getMoney(UUID uuid) {
        return getCurrency(uuid, data -> data.money);
    }

    public void setMoney(UUID uuid, double amount) {
        setCurrency(uuid, data -> data.money = amount);
    }


    public long getTokens(UUID uuid) {
        return getCurrency(uuid, data -> data.tokens);
    }

    public void setTokens(UUID uuid, long amount) {
        setCurrency(uuid, data -> data.tokens = amount);
    }


    public long getShards(UUID uuid) {
        return getCurrency(uuid, data -> data.shards);
    }

    public void setShards(UUID uuid, long amount) {
        setCurrency(uuid, data -> data.shards = amount);
    }


    public long getCredits(UUID uuid) {
        return getCurrency(uuid, data -> data.credits);
    }

    public void setCredits(UUID uuid, long amount) {
        setCurrency(uuid, data -> data.credits = amount);
    }


    public int getLevel(UUID uuid) {
        return getCurrency(uuid, data -> data.level);
    }

    public void setLevel(UUID uuid, int level) {
        setCurrency(uuid, data -> data.level = level);
    }

    public double getExperience(UUID uuid) {
        return getCurrency(uuid, data -> data.experience);
    }

    public void setExperience(UUID uuid, double xp) {
        setCurrency(uuid, data -> data.experience = xp);
    }


    public int getRebirths(UUID uuid) {
        return getCurrency(uuid, data -> data.rebirths);
    }

    public void setRebirths(UUID uuid, int rebirths) {
        setCurrency(uuid, data -> data.rebirths = rebirths);
    }


    public PlayerData getCachedData(UUID uuid) {
        return cache.get(uuid);
    }


    public CompletableFuture<Void> updateAndSave(UUID uuid, java.util.function.Consumer<PlayerData> updater) {
        PlayerData data = cache.computeIfAbsent(uuid, PlayerData::new);
        updater.accept(data);
        return savePlayerDataAsync(uuid, data);
    }


    public static class PlayerData {
        public double money;
        public long tokens;
        public long shards;
        public long credits;
        public int level;
        public double experience;
        public int rebirths;

        public PlayerData(UUID uuid) {
            this.money = 0;
            this.tokens = 0;
            this.shards = 0;
            this.credits = 0;
            this.level = 0;
            this.experience = 0;
            this.rebirths = 0;
        }

        public PlayerData(UUID uuid, double money, long tokens, long shards,
                          long credits, int level, double experience, int rebirths) {
            this.money = money;
            this.tokens = tokens;
            this.shards = shards;
            this.credits = credits;
            this.level = level;
            this.experience = experience;
            this.rebirths = rebirths;
        }
    }
}