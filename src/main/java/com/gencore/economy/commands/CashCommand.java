package com.gencore.economy.commands;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.api.MoneyAPI;
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

public class CashCommand implements CommandExecutor, TabCompleter {

    private final GenCoreEconomy plugin;
    private final MoneyAPI moneyAPI;

    public CashCommand(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.moneyAPI = plugin.getMoneyAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /cash - show own balance
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can check their balance!");
                return true;
            }
            Player player = (Player) sender;
            double balance = moneyAPI.getMoney(player.getUniqueId());
            sender.sendMessage("§aYour balance: §f" + moneyAPI.formatMoney(balance));
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
            case "remove":
                return handleTake(sender, args);

            case "set":
                return handleSet(sender, args);

            case "reset":
                return handleReset(sender, args);

            default:
                sender.sendMessage("§cUsage: /cash [bal|pay|give|take|set|reset]");
                return true;
        }
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cConsole must specify a player!");
                return true;
            }
            Player player = (Player) sender;
            double balance = moneyAPI.getMoney(player.getUniqueId());
            sender.sendMessage("§aYour balance: §f" + moneyAPI.formatMoney(balance));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        double balance = moneyAPI.getMoney(target.getUniqueId());
        sender.sendMessage("§a" + target.getName() + "'s balance: §f" + moneyAPI.formatMoney(balance));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can pay!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /cash pay <player> <amount>");
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
            double amount = NumberFormatter.parseFormattedNumber(args[2]);

            if (amount <= 0) {
                sender.sendMessage("§cAmount must be positive!");
                return true;
            }

            if (!moneyAPI.hasMoney(player.getUniqueId(), amount)) {
                sender.sendMessage("§cYou don't have enough money!");
                return true;
            }

            moneyAPI.transferMoney(player.getUniqueId(), target.getUniqueId(), amount);
            sender.sendMessage("§aYou paid " + target.getName() + " " + moneyAPI.formatMoney(amount));
            target.sendMessage("§aYou received " + moneyAPI.formatMoney(amount) + " from " + player.getName());

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount! Use formats like: 100, 1k, 5.5m, 2b");
        }

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.cash.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /cash give <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            double amount = NumberFormatter.parseFormattedNumber(args[2]);
            moneyAPI.addMoney(target.getUniqueId(), amount);
            sender.sendMessage("§aGave " + moneyAPI.formatMoney(amount) + " to " + target.getName());
            target.sendMessage("§aYou received " + moneyAPI.formatMoney(amount));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.cash.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /cash take <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            double amount = NumberFormatter.parseFormattedNumber(args[2]);
            moneyAPI.removeMoney(target.getUniqueId(), amount);
            sender.sendMessage("§aRemoved " + moneyAPI.formatMoney(amount) + " from " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.cash.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /cash set <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            double amount = NumberFormatter.parseFormattedNumber(args[2]);
            moneyAPI.setMoney(target.getUniqueId(), amount);
            sender.sendMessage("§aSet " + target.getName() + "'s balance to " + moneyAPI.formatMoney(amount));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.cash.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /cash reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        moneyAPI.resetMoney(target.getUniqueId());
        sender.sendMessage("§aReset " + target.getName() + "'s balance to $0.00");

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