package com.snowgears.mindcontrol.util;

import com.snowgears.mindcontrol.MindControl;
import com.snowgears.mindcontrol.util.HelmetSettings;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class MindControlAPI {
	
	public static boolean isMindControlHelmet(ItemStack is) {
		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(is);
		return helmetSettings != null;
	}

	public static boolean isWearingMindControlHelmet(Player player) {
		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(player.getInventory().getHelmet());
		return helmetSettings != null;
	}

	public static ItemStack createMindControlHelmet(String helmetID) {
		HelmetSettings helmetSettings = MindControl.getPlugin().getPlayerHandler().getHelmetSettings(helmetID);
		if(helmetSettings == null)
			return null;

		return helmetSettings.getHelmetItem();
	}

	public static HelmetSettings getHelmetSettings(Player player){
		return HelmetSettings.fromHelmetItem(player.getInventory().getHelmet());
	}

	public static int getUses(ItemStack helmetItem){
		if(isMindControlHelmet(helmetItem)) {
			ItemMeta helmetItemMeta = helmetItem.getItemMeta();
			PersistentDataContainer persistentData = helmetItemMeta.getPersistentDataContainer();
			int uses = persistentData.get(new NamespacedKey(MindControl.getPlugin(), "uses"), PersistentDataType.INTEGER);

			return uses;
		}
		return -1;
	}

	public static int getMaxUses(ItemStack helmetItem){
		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(helmetItem);
		if(helmetSettings == null)
			return 0;

		return helmetSettings.getMaxUses();
	}

	public static double getCaptureTime(ItemStack helmetItem){
		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(helmetItem);
		if(helmetSettings == null)
			return 0;

		return helmetSettings.getCaptureTime();
	}

	public static int getTimeBetweenUses(Player player){

		HelmetSettings helmetSettings = getHelmetSettings(player);
		if(helmetSettings == null)
			return 0;
		return helmetSettings.getTimeBetweenUses();
	}

	public static int getDistanceLimit(Player player){

		HelmetSettings helmetSettings = getHelmetSettings(player);
		if(helmetSettings == null)
			return 0;
		return helmetSettings.getDistanceLimit();
	}

	public static boolean canControlEntityType(Player player, EntityType entityType){

		HelmetSettings helmetSettings = getHelmetSettings(player);
		if(helmetSettings == null)
			return false;
		return helmetSettings.canControlEntityType(entityType);
	}

	public static Sound getStareSound(ItemStack helmetItem){
		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(helmetItem);
		if(helmetSettings == null)
			return null;

		return helmetSettings.getStareSound();
	}

	public static Sound getControlSound(ItemStack helmetItem){
		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(helmetItem);
		if(helmetSettings == null)
			return null;

		return helmetSettings.getControlSound();
	}

	public static String getHelmetID(ItemStack helmetItem){

		HelmetSettings helmetSettings = HelmetSettings.fromHelmetItem(helmetItem);
		if(helmetSettings == null)
			return null;
		return helmetSettings.getId();
	}

	//TODO come back to these and implement cooldowns
//	public static boolean isPlayerOnCoolDown(Player player) {
//		return MindControl.getPlugin().getGrapplingListener().isPlayerOnCoolDown(player);
//	}
//
//	public static void removePlayerCoolDown(Player player) {
//		MindControl.getPlugin().getGrapplingListener().removePlayerCoolDown(player);
//	}
//
//	public static void addPlayerCoolDown(final Player player, int seconds) {
//		MindControl.getPlugin().getGrapplingListener().addPlayerCoolDown(player, seconds);
//	}

	public static void breakHelmet(Player player){
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 10f, 1f);
	}

    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
}
