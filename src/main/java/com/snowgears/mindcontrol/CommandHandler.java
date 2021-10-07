package com.snowgears.mindcontrol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
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

                if ((plugin.usePerms() && player.hasPermission("mindcontrol.operator")) || player.isOp()) {
                    player.sendMessage(ChatColor.AQUA+"/" + this.getName() + " sethelmet - "+ChatColor.GRAY+"set the mind control helmet item to be the item in main hand.");
                    player.sendMessage(ChatColor.AQUA+"/" + this.getName() + " <player> - "+ChatColor.GRAY+"take control of <player>");
                    player.sendMessage(ChatColor.AQUA+"/" + this.getName() + " reload - "+ChatColor.GRAY+"reload MindControl plugin");
                }
                else{
                    sender.sendMessage("/" + this.getName() + " sethelmet - set the mind control helmet item to be the item in main hand.");
                }
            }
            else{
                sender.sendMessage("/" + this.getName() + " reload - reload MindControl plugin");
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("sethelmet")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && player.hasPermission("mindcontrol.operator")) || player.isOp()) {

                        ItemStack handItem = player.getInventory().getItemInMainHand();
                        if(handItem == null || handItem.getType() == Material.AIR){
                            player.sendMessage(ChatColor.RED+"You must have an item in your main hand to run this command.");
                            return true;
                        }
                        handItem.setAmount(1);
                        plugin.setItemCurrency(handItem);
                        sendCommandMessage("setcurrency_output", player);

                        return true;
                    }
                } else {
                    sender.sendMessage("This command must be run by a player.");
                }
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("shop.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        sendCommandMessage("not_authorized", player);
                        return true;
                    }
                    plugin.reload();
                    sendCommandMessage("reload_output", player);
                } else {
                    plugin.reload();
                    sender.sendMessage("[Shop] Reloaded plugin.");
                }

                for(Player p : Bukkit.getOnlinePlayers()){
                    if(p != null){
                        p.closeInventory();
                    }
                }
                //plugin.getShopHandler().refreshShopDisplays(null);
                plugin.getShopHandler().removeLegacyDisplays();

            }
            else if (args[0].equalsIgnoreCase("currency")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && player.hasPermission("shop.operator")) || player.isOp()) {

                        sendCommandMessage("currency_output", player);
                        sendCommandMessage("currency_output_tip", player);
                        return true;
                    }
                } else {
                    sender.sendMessage("The server is using "+plugin.getCurrencyName()+" as currency.");
                }
            }
            else if (args[0].equalsIgnoreCase("setcurrency")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && player.hasPermission("shop.operator")) || player.isOp()) {
                        if(plugin.getCurrencyType() != CurrencyType.ITEM){
                            sendCommandMessage("error_novault", player);
                            return true;
                        }
                        else{
                            ItemStack handItem = player.getInventory().getItemInMainHand();
                            if(handItem == null || handItem.getType() == Material.AIR){
                                sendCommandMessage("error_nohand", player);
                                return true;
                            }
                            handItem.setAmount(1);
                            plugin.setItemCurrency(handItem);
                            sendCommandMessage("setcurrency_output", player);
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage("The server is using "+plugin.getItemNameUtil().getName(plugin.getItemCurrency())+" as currency.");
                }
            }
            else if(args[0].equalsIgnoreCase("setgamble")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("shop.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        sendCommandMessage("not_authorized", player);
                        return true;
                    }
                    if(player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() != Material.AIR)
                        plugin.setGambleDisplayItem(player.getInventory().getItemInMainHand());
                    else {
                        sendCommandMessage("error_nohand", player);
                        return true;
                    }
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("refresh")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("shop.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        sendCommandMessage("not_authorized", player);
                        return true;
                    }
                    //plugin.getShopHandler().refreshShopDisplays(null);
                    plugin.getShopHandler().removeLegacyDisplays();
                    sendCommandMessage("itemrefresh_output", player);
                } else {
                    //plugin.getShopHandler().refreshShopDisplays(null);
                    plugin.getShopHandler().removeLegacyDisplays();
                    sender.sendMessage("[Shop] The display items on all of the shops have been refreshed.");
                }
            }
            else if (args[0].equalsIgnoreCase("itemlist")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("shop.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        sendCommandMessage("not_authorized", player);
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("add")){
                        plugin.getShopHandler().addInventoryToItemList(player.getInventory());
                        sendCommandMessage("itemlist_add", player);
                    }
                    else if(args[1].equalsIgnoreCase("remove")){
                        plugin.getShopHandler().removeInventoryFromItemList(player.getInventory());
                        sendCommandMessage("itemlist_remove", player);
                    }
                } else {
                    sender.sendMessage("[Shop] This command can only be run as a player.");
                }
            }
            else if (args[0].equalsIgnoreCase("notify")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if(args[1].equalsIgnoreCase("user")) {
                        toggleOptionAndNotifyPlayer(player, PlayerSettings.Option.SALE_USER_NOTIFICATIONS);
                    }
                    else if(args[1].equalsIgnoreCase("owner")) {
                        toggleOptionAndNotifyPlayer(player, PlayerSettings.Option.SALE_OWNER_NOTIFICATIONS);
                    }
                    else if(args[1].equalsIgnoreCase("stock")) {
                        toggleOptionAndNotifyPlayer(player, PlayerSettings.Option.STOCK_NOTIFICATIONS);
                    }
                } else {
                    sender.sendMessage("[Shop] This command can only be run as a player.");
                }
            }
        }
        return true;
    }

    private void toggleOptionAndNotifyPlayer(Player player, PlayerSettings.Option option) {
        Shop.getPlugin().getGuiHandler().toggleSettingsOption(player, option);

        switch (option) {
            case SALE_USER_NOTIFICATIONS:
                sendCommandMessage("notify_user", player);
                break;
            case SALE_OWNER_NOTIFICATIONS:
                sendCommandMessage("notify_owner", player);
                break;
            case STOCK_NOTIFICATIONS:
                sendCommandMessage("notify_stock", player);
                break;
        }
    }

    private void register()
            throws ReflectiveOperationException {
        final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);

        CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        commandMap.register(this.getName(), this);
    }
}
