package com.gencore.economy.commands;

import com.gencore.economy.GenCoreEconomy;
import com.gencore.economy.api.LevelAPI;
import com.gencore.economy.api.RebirthAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RebirthCommand implements CommandExecutor, TabCompleter {

    private final GenCoreEconomy plugin;
    private final RebirthAPI rebirthAPI;
    private final LevelAPI levelAPI;

    public RebirthCommand(GenCoreEconomy plugin) {
        this.plugin = plugin;
        this.rebirthAPI = plugin.getRebirthAPI();
        this.levelAPI = plugin.getLevelAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("gencore.rebirth")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        // /rebirth - perform rebirth
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can rebirth!");
                return true;
            }

            Player player = (Player) sender;
            int currentLevel = levelAPI.getLevel(player.getUniqueId());
            int requiredLevel = rebirthAPI.getRequiredLevel(player.getUniqueId());
            int rebirths = rebirthAPI.getRebirths(player.getUniqueId());

            if (!rebirthAPI.canRebirth(player.getUniqueId())) {
                sender.sendMessage("§c§lREBIRTH LOCKED");
                sender.sendMessage("§cCurrent Level: §f" + currentLevel);
                sender.sendMessage("§cRequired Level: §f" + requiredLevel);
                sender.sendMessage("§cLevels Needed: §f" + rebirthAPI.getLevelsUntilRebirth(player.getUniqueId()));
                return true;
            }

            rebirthAPI.rebirth(player.getUniqueId());
            sender.sendMessage("§a§l§m-----------------------");
            sender.sendMessage("§a§lREBIRTH SUCCESSFUL!");
            sender.sendMessage("§aYou are now Rebirth §f" + (rebirths + 1));
            sender.sendMessage("§aYour level has been reset to §f0");
            sender.sendMessage("§aNext rebirth requires §f" + rebirthAPI.getRequiredLevel(player.getUniqueId()) + " §alevels");
            sender.sendMessage("§a§l§m-----------------------");

            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "info":
            case "check":
                return handleInfo(sender, args);
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
            default:
                sender.sendMessage("§cUsage: /rebirth [info|give|take|set|reset]");
                return true;
        }
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        Player target;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cConsole must specify a player!");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }
        }

        int rebirths = rebirthAPI.getRebirths(target.getUniqueId());
        int currentLevel = levelAPI.getLevel(target.getUniqueId());
        int requiredLevel = rebirthAPI.getRequiredLevel(target.getUniqueId());
        int levelsNeeded = rebirthAPI.getLevelsUntilRebirth(target.getUniqueId());
        boolean canRebirth = rebirthAPI.canRebirth(target.getUniqueId());

        sender.sendMessage("§a§l" + target.getName() + "'s Rebirth Info");
        sender.sendMessage("§aRebirths: §f" + rebirths);
        sender.sendMessage("§aCurrent Level: §f" + currentLevel);
        sender.sendMessage("§aRequired Level: §f" + requiredLevel);
        sender.sendMessage("§aLevels Needed: §f" + levelsNeeded);
        sender.sendMessage("§aCan Rebirth: " + (canRebirth ? "§aYes" : "§cNo"));

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.rebirth.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /rebirth give <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            rebirthAPI.addRebirths(target.getUniqueId(), amount);
            sender.sendMessage("§aGave " + amount + " rebirths to " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.rebirth.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /rebirth take <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            int current = rebirthAPI.getRebirths(target.getUniqueId());
            rebirthAPI.setRebirths(target.getUniqueId(), Math.max(0, current - amount));
            sender.sendMessage("§aRemoved " + amount + " rebirths from " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.rebirth.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /rebirth set <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            rebirthAPI.setRebirths(target.getUniqueId(), amount);
            sender.sendMessage("§aSet " + target.getName() + "'s rebirths to " + amount);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gencore.rebirth.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /rebirth reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        rebirthAPI.resetRebirths(target.getUniqueId());
        sender.sendMessage("§aReset " + target.getName() + "'s rebirths to 0");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.stream.Stream.of("info", "give", "take", "set", "reset")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            return null;
        }
        return java.util.List.of();
    }
}