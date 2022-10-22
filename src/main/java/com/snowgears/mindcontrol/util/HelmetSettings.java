package com.snowgears.mindcontrol.util;

import com.snowgears.mindcontrol.MindControl;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelmetSettings {

    private String id;
    private int maxUses;

    private double captureTime;
    private int distanceLimit;
    private int timeLimit;
    private int timeBetweenUses;

    private Sound stareSound;
    private Sound controlSound;

    private BarColor progressBarColor;
    private BarStyle progressBarStyle;

    private ItemStack helmetItem;

    private HashMap<EntityType, Boolean> entityTypes = new HashMap<>();

    public HelmetSettings(String id,
                          int maxUses,
                          double captureTime,
                          int distanceLimit,
                          int timeLimit,
                          int timeBetweenUses,
                          Sound stareSound,
                          Sound controlSound,
                          BarColor progressBarColor,
                          BarStyle progressBarStyle){

        this.id = id;
        this.maxUses = maxUses;
        this.captureTime = captureTime;
        this.distanceLimit = distanceLimit;
        this.timeLimit = timeLimit;
        this.timeBetweenUses = timeBetweenUses;
        this.stareSound = stareSound;
        this.controlSound = controlSound;
        this.progressBarColor = progressBarColor;
        this.progressBarStyle = progressBarStyle;
    }

    public void setEntityList(boolean isBlackList, List<EntityType> entityTypeList){
        if(isBlackList){
            //add all entities that are a living entity
            List<EntityType> entities = new ArrayList<>();
            for(EntityType entityType : EntityType.values()){
                if(entityType.isAlive() && entityType != EntityType.ARMOR_STAND){
                    entities.add(entityType);
                }
            }
            // remove all blacklisted entities from the list
            for(EntityType entityType : entityTypeList){
                entities.remove(entityType);
            }

            //store list in the hashmap as entries
            for(EntityType entityType : entities){
                this.entityTypes.put(entityType, true);
            }
        }
        else{
            // store all whitelisted entities in hashmap as entries
            for(EntityType entityType : entityTypeList){
                this.entityTypes.put(entityType, true);
            }
        }
    }

    public ItemStack getHelmetItem() {
        return helmetItem;
    }

    public void setHelmetItem(ItemStack helmetItem) {
        this.helmetItem = helmetItem;
    }

    public String getId() {
        return id;
    }

    public int getMaxUses(){
        return maxUses;
    }

    public double getCaptureTime(){
        return captureTime;
    }

    public int getDistanceLimit() {
        return distanceLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getTimeBetweenUses() {
        return timeBetweenUses;
    }

    public Sound getStareSound() {
        return stareSound;
    }

    public Sound getControlSound() {
        return controlSound;
    }

    public BarColor getProgressBarColor() {
        return progressBarColor;
    }

    public BarStyle getProgressBarStyle() {
        return progressBarStyle;
    }

    public boolean canControlEntityType(EntityType entityType){
        return entityTypes.containsKey(entityType);
    }

    public static HelmetSettings fromHelmetItem(ItemStack helmetItem){
        try {
            ItemMeta im = helmetItem.getItemMeta();
            PersistentDataContainer persistentData = im.getPersistentDataContainer();

            String helmetID = persistentData.get(new NamespacedKey(MindControl.getPlugin(), "id"), PersistentDataType.STRING);
            return MindControl.getPlugin().getPlayerHandler().getHelmetSettings(helmetID);
        } catch (Exception e) {
            return null;
        }
    }
}
