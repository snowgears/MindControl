package com.snowgears.mindcontrol.entity;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public class PlayerData {

    private UUID playerUUID;
    private UUID fakePlayerUUID;
    private ItemStack[] oldInventoryContents;
    private ItemStack[] oldArmorContents;
    private Collection<PotionEffect> oldPotionEffects;
    private Location oldLocation;
    private GameMode oldGameMode;
    private double oldHealth;
    private double oldMaxHealth;
    private int oldHunger;
    private int oldExperience;
    private int oldRemainingAir;
    private int oldFireTicks;
    private int beamTaskID;
    private int timerTaskID;

    public PlayerData(Player player) {
        //TODO handle saving and loading from file here
        this.playerUUID = player.getUniqueId();
        this.oldInventoryContents = player.getInventory().getContents();
        this.oldArmorContents = player.getInventory().getArmorContents();
        this.oldLocation = player.getLocation().clone();
        this.oldGameMode = player.getGameMode();
        this.oldHealth = player.getHealth();
        this.oldMaxHealth = player.getMaxHealth();
        this.oldHunger = player.getFoodLevel();
        this.oldExperience = player.getTotalExperience();
        this.oldRemainingAir = player.getRemainingAir();
        this.oldFireTicks = player.getFireTicks();
        this.oldPotionEffects = player.getActivePotionEffects();
    }

    public UUID getUUID(){
        return playerUUID;
    }

    public void setFakePlayerUUID(UUID fakePlayerUUID) {
        this.fakePlayerUUID = fakePlayerUUID;
    }

    public UUID getFakePlayerUUID(){
        return fakePlayerUUID;
    }

    public Location getOldLocation(){
        return oldLocation;
    }

    public ItemStack[] getOldArmorContents(){
        return oldArmorContents;
    }

    public void setBeamTaskID(int beamTaskID){
        this.beamTaskID = beamTaskID;
    }

//    public int getBeamTaskID(){
//        return beamTaskID;
//    }
//
//    public int getTimerTaskID(){
//        return timerTaskID;
//    }

    public void setTimerTaskID(int timerTaskID){
        this.timerTaskID = timerTaskID;
    }

    //TODO implement save method
    private void saveToFile(){

    }

    @Override
    public PlayerData clone(){
        PlayerData playerData = new PlayerData(Bukkit.getPlayer(this.playerUUID));
        playerData.setFakePlayerUUID(this.getFakePlayerUUID());
        playerData.setBeamTaskID(this.beamTaskID);
        playerData.setTimerTaskID(this.timerTaskID);
        return playerData;
    }

    //this method is called when the player data is returned to the controlling player
    public void apply(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        if(livingEntity instanceof Player){
            Player player = (Player)livingEntity;

            player.getInventory().setContents(oldInventoryContents);
            player.getInventory().setArmorContents(oldArmorContents);
            player.setGameMode(oldGameMode);
            player.setFoodLevel(this.oldHunger);
            if(this.oldExperience < 0)
                this.oldExperience = 0;
            player.setTotalExperience(this.oldExperience);
            livingEntity.teleport(oldLocation);
        }

        Bukkit.getServer().getScheduler().cancelTask(beamTaskID);
        Bukkit.getServer().getScheduler().cancelTask(timerTaskID);

        //TODO come back to this
//        livingEntity.setMaxHealth(this.oldMaxHealth);
//        livingEntity.setHealth(this.oldHealth);
//        livingEntity.setRemainingAir(this.oldRemainingAir);
//        livingEntity.setFireTicks(this.oldFireTicks);

        for(PotionEffect effect : oldPotionEffects) {
            livingEntity.addPotionEffect(effect);
        }
    }
}
