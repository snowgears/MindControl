package com.snowgears.mindcontrol.util;

import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileCreator {

    private static List<EntityType> canFly = new ArrayList();
    private static List<EntityType> breathWater = new ArrayList();


    public FileCreator() {
        initLists();
        createFile();
    }

    public static void main(String[] args){
        initLists();
        createFile();
    }

    private static void initLists(){
        canFly.add(EntityType.ALLAY);
        canFly.add(EntityType.BAT);
        canFly.add(EntityType.BEE);
        canFly.add(EntityType.BLAZE);
        canFly.add(EntityType.ENDER_DRAGON);
        canFly.add(EntityType.GHAST);
        canFly.add(EntityType.PARROT);
        canFly.add(EntityType.PHANTOM);
        canFly.add(EntityType.VEX);
        canFly.add(EntityType.WITHER);

        breathWater.add(EntityType.AXOLOTL);
        breathWater.add(EntityType.COD);
        breathWater.add(EntityType.DOLPHIN);
        breathWater.add(EntityType.ELDER_GUARDIAN);
        breathWater.add(EntityType.FROG);
        breathWater.add(EntityType.GLOW_SQUID);
        breathWater.add(EntityType.GUARDIAN);
        breathWater.add(EntityType.PUFFERFISH);
        breathWater.add(EntityType.SALMON);
        breathWater.add(EntityType.SQUID);
        breathWater.add(EntityType.TADPOLE);
        breathWater.add(EntityType.TROPICAL_FISH);
        breathWater.add(EntityType.TURTLE);
    }

    private static void createFile(){
        //System.out.println("[Shop] saving shops for player - "+player.toString());
        try {

            String owner = null;
            File currentFile = new File("entities.yml");

            //owner = currentFile.getName().substring(0, currentFile.getName().length()-4); //remove .yml

            if (!currentFile.exists()) // file doesn't exist
                currentFile.createNewFile();
            else{
                currentFile.delete();
                currentFile.createNewFile();
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(currentFile);

            List<String> entities = new ArrayList<>();
            for(EntityType entityType : EntityType.values()){
                if(entityType.isAlive() && entityType != EntityType.ARMOR_STAND){
                    entities.add(entityType.toString());
                }
            }
            Collections.sort(entities);

            for (String entityType : entities) {

                config.set("entity." + entityType + ".speed", 0.5);
                if(canFly.contains(EntityType.valueOf(entityType))){
                    config.set("entity." + entityType + ".fly", true);
                }
                else{
                    config.set("entity." + entityType + ".fly", false);
                }

                if(breathWater.contains(EntityType.valueOf(entityType))){
                    config.set("entity." + entityType + ".drown", false);
                    config.set("entity." + entityType + ".suffocate", true);
                }
                else{
                    config.set("entity." + entityType + ".drown", true);
                    config.set("entity." + entityType + ".suffocate", false);
                }


                config.set("entity." + entityType + ".action.enabled", true);

                ArrayList<String> potentialSounds = new ArrayList<>();
                for(Sound sound : Sound.values()){
                    if(sound.toString().startsWith("ENTITY_"+entityType)){
                        potentialSounds.add(sound.toString());
                    }
                }


                config.set("entity." + entityType + ".action.sound", "?");
                config.set("entity." + entityType + ".action.potentialSound", potentialSounds);

                config.set("entity." + entityType + ".attack.enabled", true);
                config.set("entity." + entityType + ".attack.sound", "?");
                config.set("entity." + entityType + ".attack.damage", 2);

            }
            config.save(currentFile);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}