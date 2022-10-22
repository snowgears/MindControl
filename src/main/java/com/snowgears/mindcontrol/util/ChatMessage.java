package com.snowgears.mindcontrol.util;

import com.snowgears.mindcontrol.MindControl;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class ChatMessage {

    private static HashMap<String, String> messageMap = new HashMap<String, String>();
    private static YamlConfiguration chatConfig;

    public ChatMessage(MindControl plugin) {

        //load up the chat config file and make sure its updated with any new variables
        File chatConfigFile = new File(plugin.getDataFolder(), "chatConfig.yml");
        if (!chatConfigFile.exists()) {
            chatConfigFile.getParentFile().mkdirs();
            plugin.copy(plugin.getResource("chatConfig.yml"), chatConfigFile);
        }
        try {
            ConfigUpdater.update(plugin, "chatConfig.yml", chatConfigFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        chatConfig = YamlConfiguration.loadConfiguration(chatConfigFile);

        loadMessagesFromConfig();
    }

    public static void sendMessage(Player player, String key, String subKey, ItemStack helmetItem, String playerName) {
        String toSend = getMessage(key, subKey, helmetItem, playerName);

        if(toSend == null || toSend.isEmpty())
            return;
        player.sendMessage(toSend);
    }

    public static void sendMessageConsole(CommandSender console, String key, String subKey, ItemStack helmetItem, String playerName) {
        String toSend = getMessage(key, subKey, helmetItem, playerName);

        if(toSend == null || toSend.isEmpty())
            return;
        toSend = ChatColor.stripColor(toSend);
        console.sendMessage(toSend);
    }

    public static String getMessage(String key, String subKey, ItemStack helmetItem, String playerName) {
        String message = "";
        String mainKey = key;
        if (subKey != null) {
            mainKey = key + "_" + subKey;
        }

        if (messageMap.containsKey(mainKey))
            message = messageMap.get(mainKey);
        else
            return message;

        message = formatMessage(message, helmetItem, playerName);
        return message;
    }

    public static String getUnformattedMessage(String key, String subKey) {
        String message;
        if (subKey != null)
            message = messageMap.get(key + "_" + subKey);
        else
            message = messageMap.get(key);
        return message;
    }


    public static String formatMessage(String unformattedMessage, ItemStack helmetItem, String playerName){
        if(unformattedMessage == null) {
            loadMessagesFromConfig();
            return "";
        }
        unformattedMessage = unformattedMessage.replace("\\n", System.lineSeparator());

        if(helmetItem != null) {
            unformattedMessage = unformattedMessage.replace("[uses]", "" + MindControlAPI.getUses(helmetItem));
            unformattedMessage = unformattedMessage.replace("[max_uses]", "" + MindControlAPI.getMaxUses(helmetItem));
            unformattedMessage = unformattedMessage.replace("[helmet_id]", "" + MindControlAPI.getHelmetID(helmetItem));
        }

        if(playerName != null){
            unformattedMessage = unformattedMessage.replace("[player]", "" + playerName);
        }

        unformattedMessage = unformattedMessage.replace("[command]", "" + MindControl.getPlugin().getCommandAlias());

        unformattedMessage = ChatColor.translateAlternateColorCodes('&', unformattedMessage);
        return unformattedMessage;
    }

    private static void loadMessagesFromConfig() {

        for(String section : chatConfig.getConfigurationSection("message").getKeys(false)){
            for(String key : chatConfig.getConfigurationSection("message."+section).getKeys(false)){
                //System.out.println(section + "_"+ key + "  --  "+chatConfig.getString("message."+section+"."+key));
                putMessageInMap(section, key, chatConfig.getString("message."+section+"."+key));
            }
        }
    }

    private static void putMessageInMap(String key, String subKey, String message) {
        String mainKey = key;
        if (subKey != null) {
            mainKey = key + "_" + subKey;
        }
        messageMap.put(mainKey, message);
    }
}