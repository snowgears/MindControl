package com.snowgears.mindcontrol.entity;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
    private int timerTaskID;

    private double oldMovementSpeed;
    private double oldAttackDamage;

    private boolean oldAllowFlight;
    private boolean oldIsFlying;
    private float oldFlySpeed;

    public PlayerData(Player player) {
        //TODO handle saving and loading from file here
        this.playerUUID = player.getUniqueId();
        this.oldInventoryContents = player.getInventory().getContents();
        this.oldArmorContents = player.getInventory().getArmorContents();
        this.oldLocation = player.getLocation().clone();
        this.oldGameMode = player.getGameMode();
        this.oldHealth = player.getHealth();
        this.oldHunger = player.getFoodLevel();
        this.oldExperience = player.getTotalExperience();
        this.oldRemainingAir = player.getRemainingAir();
        this.oldFireTicks = player.getFireTicks();
        this.oldPotionEffects = player.getActivePotionEffects();

        AttributeInstance playerMaxHealthInstance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if(playerMaxHealthInstance != null){
            this.oldMaxHealth = playerMaxHealthInstance.getBaseValue();
        }
        AttributeInstance playerSpeedInstance = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if(playerSpeedInstance != null){
            this.oldMovementSpeed = playerSpeedInstance.getBaseValue();
        }
        AttributeInstance playerAttackDamageInstance = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (playerAttackDamageInstance != null) {
            this.oldAttackDamage = playerAttackDamageInstance.getBaseValue();
        }

        this.oldAllowFlight = player.getAllowFlight();
        this.oldIsFlying = player.isFlying();
        this.oldFlySpeed = player.getFlySpeed();
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
        playerData.setTimerTaskID(this.timerTaskID);
        return playerData;
    }

    //this method is called when the player data is returned to the controlling player
    public void apply(LivingEntity livingEntity) {
        if(livingEntity instanceof Player){
            Player player = (Player)livingEntity;

            player.getInventory().setContents(oldInventoryContents);
            player.getInventory().setArmorContents(oldArmorContents);
            player.setGameMode(oldGameMode);
            player.setFoodLevel(this.oldHunger);
            if(this.oldExperience < 0)
                this.oldExperience = 0;
            player.setTotalExperience(this.oldExperience);

            player.setAllowFlight(oldAllowFlight);
            player.setFlying(oldIsFlying);
            player.setFlySpeed(oldFlySpeed);

            player.setArrowsInBody(0);
            player.teleport(oldLocation);
        }

        Bukkit.getServer().getScheduler().cancelTask(timerTaskID);

        //TODO come back to this
//        livingEntity.setMaxHealth(this.oldMaxHealth);
//        livingEntity.setHealth(this.oldHealth);
//        livingEntity.setRemainingAir(this.oldRemainingAir);
//        livingEntity.setFireTicks(this.oldFireTicks);

        AttributeInstance playerMaxHealthInstance = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if(playerMaxHealthInstance != null){
            playerMaxHealthInstance.setBaseValue(oldMaxHealth);
        }
        AttributeInstance playerSpeedInstance = livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if(playerSpeedInstance != null){
            playerSpeedInstance.setBaseValue(oldMovementSpeed);
        }
        AttributeInstance playerAttackDamageInstance = livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (playerAttackDamageInstance != null) {
            playerAttackDamageInstance.setBaseValue(oldAttackDamage);
        }

        livingEntity.setHealth(this.oldHealth);

        for(PotionEffect effect : oldPotionEffects) {
            livingEntity.addPotionEffect(effect);
        }
    }

    public double getHealth(){
        return oldHealth;
    }
}
