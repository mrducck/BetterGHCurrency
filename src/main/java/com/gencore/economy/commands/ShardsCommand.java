package com.gencore.economy.commands;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.api.ShardAPI;
import com.gencore.economy.util.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Shards command follows the same pattern
public class ShardsCommand implements CommandExecutor, TabCompleter {

    private final GenCoreEconomy plugin;
    private final ShardAPI shardAPI;

    public ShardsCommand(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.shardAPI = plugin.getShardAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can check their balance!");
                return true;
            }
            Player player = (Player) sender;
            long balance = shardAPI.getShards(player.getUniqueId());
            sender.sendMessage("§aYour shards: §f" + String.format("%,d", balance));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "bal":
            case "balance":
                return handleBalance(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "reset":
                return handleReset(sender, args);
            default:
                sender.sendMessage("§cUsage: /shards [bal|pay|give|take|set|reset]");
                return true;
        }
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.shards")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cConsole must specify a player!");
                return true;
            }
            Player player = (Player) sender;
            long balance = shardAPI.getShards(player.getUniqueId());
            sender.sendMessage("§aYour shards: §f" + String.format("%,d", balance));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        long balance = shardAPI.getShards(target.getUniqueId());
        sender.sendMessage("§a" + target.getName() + "'s shards: §f" + String.format("%,d", balance));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.shards")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can pay!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /shards pay <player> <amount>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage("§cYou cannot pay yourself!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);

            if (amount <= 0) {
                sender.sendMessage("§cAmount must be positive!");
                return true;
            }

            if (!shardAPI.hasShards(player.getUniqueId(), amount)) {
                sender.sendMessage("§cYou don't have enough shards!");
                return true;
            }

            shardAPI.transferShards(player.getUniqueId(), target.getUniqueId(), amount);
            sender.sendMessage("§aYou paid " + target.getName() + " " + String.format("%,d", amount) + " shards");
            target.sendMessage("§aYou received " + String.format("%,d", amount) + " shards from " + player.getName());

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount! Use formats like: 100, 1k, 5m, 2b");
        }

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.shards.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /shards give <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);
            shardAPI.addShards(target.getUniqueId(), amount);
            sender.sendMessage("§aGave " + String.format("%,d", amount) + " shards to " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.shards.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /shards take <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);
            shardAPI.removeShards(target.getUniqueId(), amount);
            sender.sendMessage("§aRemoved " + String.format("%,d", amount) + " shards from " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.shards.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /shards set <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);
            shardAPI.setShards(target.getUniqueId(), amount);
            sender.sendMessage("§aSet " + target.getName() + "'s shards to " + String.format("%,d", amount));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.shards.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /shards reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        shardAPI.resetShards(target.getUniqueId());
        sender.sendMessage("§aReset " + target.getName() + "'s shards to 0");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.stream.Stream.of("bal", "pay", "give", "take", "set", "reset")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("pay")) {
            return null; // Return player names
        }
        return java.util.List.of();
    }
}