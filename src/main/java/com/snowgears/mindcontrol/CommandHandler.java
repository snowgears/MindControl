package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.util.ChatMessage;
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
                    ChatMessage.sendMessage(player, "help", "giveSelf", null, player.getName());
                    ChatMessage.sendMessage(player, "help", "giveOther", null, player.getName());
                    ChatMessage.sendMessage(player, "help", "reload", null, player.getName());
                    return true;
                }
            }
            //these are commands that can be executed from the console
            else{
                ChatMessage.sendMessageConsole(sender, "help", "giveOther", null, null);
                ChatMessage.sendMessageConsole(sender, "help", "reload", null, null);
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("mindcontrol.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        ChatMessage.sendMessage(player, "error", "permCommand", null, player.getName());
                        return true;
                    }
                    plugin.reload();
                    ChatMessage.sendMessage(player, "info", "reload", null, player.getName());
                } else {
                    plugin.reload();
                    ChatMessage.sendMessageConsole(sender, "info", "reload", null, null);
                    return true;
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("mindcontrol.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        ChatMessage.sendMessage(player, "error", "permCommand", null, player.getName());
                        return true;
                    }

                    String helmetID = args[1];
                    HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(helmetID);
                    if(helmetSettings == null){
                        String message = ChatMessage.getMessage("error", "helmetID", null, player.getName());
                        message = message.replace("[helmet_id]", helmetID);
                        if(message != null && !message.isEmpty()){
                            player.sendMessage(message);
                        }
                        return true;
                    }
                    player.getInventory().addItem(helmetSettings.getHelmetItem());
                    //player.sendMessage(ChatColor.GREEN+"Gave mind control helmet <"+helmetID+"> to "+player.getName());
                    ChatMessage.sendMessage(player, "info", "giveHelmet", helmetSettings.getHelmetItem(), player.getName());
                    return true;
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("mindcontrol.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        ChatMessage.sendMessage(player, "error", "permCommand", null, player.getName());
                        return true;
                    }

                    String helmetID = args[1];
                    HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(helmetID);
                    if(helmetSettings == null){
                        String message = ChatMessage.getMessage("error", "helmetID", null, player.getName());
                        message = message.replace("[helmet_id]", helmetID);
                        if(message != null && !message.isEmpty()){
                            player.sendMessage(message);
                        }
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        //player.sendMessage(ChatColor.RED+"No player found online with name: "+args[2]);
                        ChatMessage.sendMessage(player, "error", "playerOnline", null, args[2]);
                        return true;
                    }
                    playerToGive.getInventory().addItem(helmetSettings.getHelmetItem());
//                    player.sendMessage(ChatColor.GREEN+"Gave mind control helmet <"+helmetSettings+"> to "+args[2]);
//                    playerToGive.sendMessage(ChatColor.GREEN+player.getName()+" gave you a mind control helmet <"+helmetID+">");
//
                    ChatMessage.sendMessage(player, "info", "giveHelmet", helmetSettings.getHelmetItem(), playerToGive.getName());
                    ChatMessage.sendMessage(playerToGive, "info", "getHelmet", helmetSettings.getHelmetItem(), null);
                    return true;
                }
                else {
                    String helmetID = args[1];
                    HelmetSettings helmetSettings = plugin.getPlayerHandler().getHelmetSettings(helmetID);
                    if(helmetSettings == null){
                        String message = ChatMessage.getMessage("error", "helmetID", null, null);
                        message = message.replace("[helmet_id]", helmetID);
                        if(message != null && !message.isEmpty()){
                            message = ChatColor.stripColor(message);
                            sender.sendMessage(message);
                        }
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        //sender.sendMessage("No player found online with name: "+args[2]);
                        ChatMessage.sendMessageConsole(sender, "error", "playerOnline", null, args[2]);
                        return true;
                    }
                    playerToGive.getInventory().addItem(helmetSettings.getHelmetItem());
                    //sender.sendMessage("Gave mind control helmet <"+helmetID+"> to "+args[2]);
                    ChatMessage.sendMessageConsole(sender, "info", "giveHelmet", helmetSettings.getHelmetItem(), playerToGive.getName());
                    //playerToGive.sendMessage(ChatColor.GREEN+"The server has given you a mind control helmet <"+helmetID+">");
                    ChatMessage.sendMessage(playerToGive, "info", "getHelmet", helmetSettings.getHelmetItem(), null);
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
