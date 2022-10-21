package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.util.HelmetSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandHandler extends BukkitCommand {

    private MindControl plugin;

    public CommandHandler(MindControl instance, String permission, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.setPermission(permission);
        plugin = instance;
        try {
            register();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                //these are commands only operators have access to
                if (player.hasPermission("mindcontrol.operator") || player.isOp()) {
                    player.sendMessage("/"+this.getName()+" give <helmet_id> - give yourself a mind control helmet");
                    player.sendMessage("/"+this.getName()+" give <helmet_id> <player> - give player a mind control helmet");
                    return true;
                }
            }
            //these are commands that can be executed from the console
            else{
                sender.sendMessage("/"+this.getName()+" give <helmet_id> <player> - give player a mind control helmet");
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("mindcontrol.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(ChatColor.RED+"You are not authorized to use this command.");
                        return true;
                    }
                    plugin.reload();
                    player.sendMessage(ChatColor.GREEN+"Mind Control has been reloaded.");
                } else {
                    plugin.reload();
                    sender.sendMessage("[MindControl] Reloaded plugin.");
                    return true;
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("mindcontrol.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(ChatColor.RED+"You are not authorized to use this command.");
                        return true;
                    }

                    String helmetID = args[1];
                    HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(helmetID);
                    if(helmetSettings == null){
                        player.sendMessage(ChatColor.RED+"No mind control helmet found with id: "+helmetID);
                        return true;
                    }
                    player.getInventory().addItem(helmetSettings.getHelmetItem());
                    player.sendMessage(ChatColor.GREEN+"Gave mind control helmet <"+helmetID+"> to "+player.getName());
                    return true;
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("mindcontrol.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(ChatColor.RED+"You are not authorized to use this command.");
                        return true;
                    }

                    String helmetID = args[1];
                    HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(helmetID);
                    if(helmetSettings == null){
                        player.sendMessage(ChatColor.RED+"No mind control helmet found with id: "+helmetID);
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        player.sendMessage(ChatColor.RED+"No player found online with name: "+args[2]);
                        return true;
                    }
                    playerToGive.getInventory().addItem(helmetSettings.getHelmetItem());
                    player.sendMessage(ChatColor.GREEN+"Gave mind control helmet <"+helmetSettings+"> to "+args[2]);
                    playerToGive.sendMessage(ChatColor.GREEN+player.getName()+" gave you a mind control helmet <"+helmetID+">");
                    return true;
                }
                else {
                    String helmetID = args[1];
                    HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(helmetID);
                    if(helmetSettings == null){
                        sender.sendMessage("No mind control helmet found with id: "+helmetID);
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        sender.sendMessage("No player found online with name: "+args[2]);
                        return true;
                    }
                    playerToGive.getInventory().addItem(helmetSettings.getHelmetItem());
                    sender.sendMessage("Gave mind control helmet <"+helmetID+"> to "+args[2]);
                    playerToGive.sendMessage(ChatColor.GREEN+"The server has given you a mind control helmet <"+helmetID+">");
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> results = new ArrayList<>();
        if (args.length == 0) {
            results.add(this.getName());
        }
        else if (args.length == 1) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("mindcontrol.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            if(showOperatorCommands){
                results.add("give");
                results.add("reload");
            }
            return sortedResults(args[0], results);
        }
        else if (args.length == 2) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("mindcontrol.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            if(showOperatorCommands && args[0].equalsIgnoreCase("give")){
                for(String helmetID : plugin.getPlayerHandler().getHelmetIDs()) {
                    results.add(helmetID);
                }
            }
            return sortedResults(args[1], results);
        }
        else if (args.length == 3) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("mindcontrol.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(args[1]);

            if(showOperatorCommands && helmetSettings != null){
                results.add("<player name>");
            }
            return sortedResults(args[1], results);
        }
        return results;
    }

    private void register()
            throws ReflectiveOperationException {
        final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);

        CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        commandMap.register(this.getName(), this);
    }

    // Sorts possible results to provide true tab auto complete based off of what is already typed.
    public List <String> sortedResults(String arg, List<String> results) {
        final List <String> completions = new ArrayList < > ();
        StringUtil.copyPartialMatches(arg, results, completions);
        Collections.sort(completions);
        results.clear();
        for (String s: completions) {
            results.add(s);
        }
        return results;
    }
}
