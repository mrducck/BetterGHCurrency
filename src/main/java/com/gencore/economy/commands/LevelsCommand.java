package com.gencore.economy.commands;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.api.LevelAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LevelsCommand implements CommandExecutor, TabCompleter {

    private final GenCoreEconomy plugin;
    private final LevelAPI levelAPI;

    public LevelsCommand(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.levelAPI = plugin.getLevelAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can check their level!");
                return true;
            }
            Player player = (Player) sender;
            int level = levelAPI.getLevel(player.getUniqueId());
            double xp = levelAPI.getExperience(player.getUniqueId());
            sender.sendMessage("§aYour level: §f" + level);
            sender.sendMessage("§aYour XP: §f" + String.format("%.2f", xp));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "check":
            case "view":
                return handleCheck(sender, args);
            case "give":
            case "add":
                return handleGive(sender, args);
            case "take":
            case "remove":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "addxp":
                return handleAddXP(sender, args);
            default:
                sender.sendMessage("§cUsage: /levels [check|give|take|set|reset|addxp]");
                return true;
        }
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /levels check <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        int level = levelAPI.getLevel(target.getUniqueId());
        double xp = levelAPI.getExperience(target.getUniqueId());
        sender.sendMessage("§a" + target.getName() + "'s level: §f" + level);
        sender.sendMessage("§a" + target.getName() + "'s XP: §f" + String.format("%.2f", xp));
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.levels.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /levels give <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            levelAPI.addLevels(target.getUniqueId(), amount);
            sender.sendMessage("§aGave " + amount + " levels to " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.levels.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /levels take <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            levelAPI.removeLevels(target.getUniqueId(), amount);
            sender.sendMessage("§aRemoved " + amount + " levels from " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.levels.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /levels set <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            levelAPI.setLevel(target.getUniqueId(), amount);
            sender.sendMessage("§aSet " + target.getName() + "'s level to " + amount);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.levels.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /levels reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        levelAPI.resetLevel(target.getUniqueId());
        sender.sendMessage("§aReset " + target.getName() + "'s level to 0");

        return true;
    }

    private boolean handleAddXP(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.levels.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /levels addxp <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            levelAPI.addExperience(target.getUniqueId(), amount);
            sender.sendMessage("§aAdded " + String.format("%.2f", amount) + " XP to " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.stream.Stream.of("check", "give", "take", "set", "reset", "addxp")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            return null;
        }
        return java.util.List.of();
    }
}