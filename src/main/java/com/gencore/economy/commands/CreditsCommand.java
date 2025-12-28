package com.gencore.economy.commands;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.api.CreditAPI;
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

public class CreditsCommand implements CommandExecutor, TabCompleter {

    private final GenCoreEconomy plugin;
    private final CreditAPI creditAPI;

    public CreditsCommand(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.creditAPI = plugin.getCreditAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can check their balance!");
                return true;
            }
            Player player = (Player) sender;
            long balance = creditAPI.getCredits(player.getUniqueId());
            sender.sendMessage("§aYour credits: §f" + String.format("%,d", balance));
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
                sender.sendMessage("§cUsage: /credits [bal|pay|give|take|set|reset]");
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
            long balance = creditAPI.getCredits(player.getUniqueId());
            sender.sendMessage("§aYour credits: §f" + String.format("%,d", balance));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        long balance = creditAPI.getCredits(target.getUniqueId());
        sender.sendMessage("§a" + target.getName() + "'s credits: §f" + String.format("%,d", balance));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can pay!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /credits pay <player> <amount>");
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

            if (!creditAPI.hasCredits(player.getUniqueId(), amount)) {
                sender.sendMessage("§cYou don't have enough credits!");
                return true;
            }

            creditAPI.transferCredits(player.getUniqueId(), target.getUniqueId(), amount);
            sender.sendMessage("§aYou paid " + target.getName() + " " + String.format("%,d", amount) + " credits");
            target.sendMessage("§aYou received " + String.format("%,d", amount) + " credits from " + player.getName());

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.credits.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /credits give <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);
            creditAPI.addCredits(target.getUniqueId(), amount);
            sender.sendMessage("§aGave " + String.format("%,d", amount) + " credits to " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.credits.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /credits take <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);
            creditAPI.removeCredits(target.getUniqueId(), amount);
            sender.sendMessage("§aRemoved " + String.format("%,d", amount) + " credits from " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.credits.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /credits set <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            long amount = NumberFormatter.parseFormattedLong(args[2]);
            creditAPI.setCredits(target.getUniqueId(), amount);
            sender.sendMessage("§aSet " + target.getName() + "'s credits to " + String.format("%,d", amount));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.credits.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /credits reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        creditAPI.resetCredits(target.getUniqueId());
        sender.sendMessage("§aReset " + target.getName() + "'s credits to 0");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.stream.Stream.of("bal", "pay", "give", "take", "set", "reset")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("pay")) {
            return null;
        }
        return java.util.List.of();
    }
}