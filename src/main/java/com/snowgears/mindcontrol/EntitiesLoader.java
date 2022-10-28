package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.util.EntityControlSettings;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

public class EntitiesLoader {

    private MindControl plugin;
    private File entitiesFile;

    public EntitiesLoader(MindControl plugin){
        this.plugin = plugin;
        this.entitiesFile = new File(plugin.getDataFolder(), "entities.yml");

        loadEntities();
    }

    private void loadEntities() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(entitiesFile);
        if (config.getConfigurationSection("entities") == null) {
            plugin.getLogger().log(Level.SEVERE, "Entities file could not be loaded.");
            return;
        }

        Set<String> allEntityTypes = config.getConfigurationSection("entities").getKeys(false);

        for (String entityTypeString : allEntityTypes) {
            EntityType entityType = null;
            try {
                entityType = EntityType.valueOf(entityTypeString);

                double speed = config.getDouble("entities." + entityTypeString + ".speed");
                boolean fly = config.getBoolean("entities." + entityTypeString + ".fly");
                boolean drown = config.getBoolean("entities." + entityTypeString + ".drown");
                boolean suffocate = config.getBoolean("entities." + entityTypeString + ".suffocate");
                boolean actionEnabled = config.getBoolean("entities." + entityTypeString + ".action.enabled");

                String actionSoundString = config.getString("entities." + entityTypeString + ".action.sound");
                Sound actionSound = null;
                try {
                    actionSound = Sound.valueOf(actionSoundString);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "unrecognized sound in entities section " + entityTypeString + ": " + actionSoundString);
                }

                boolean attackEnabled = config.getBoolean("entities." + entityTypeString + ".attack.enabled");

                String attackSoundString = config.getString("entities." + entityTypeString + ".attack.sound");
                Sound attackSound = null;
                try {
                    attackSound = Sound.valueOf(attackSoundString);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "unrecognized sound in entities section " + entityTypeString + ": " + attackSoundString);
                }

                double attackDamage = config.getDouble("entities." + entityTypeString + ".attack.damage");

                EntityControlSettings entityControlSettings = new EntityControlSettings(entityType, speed, fly, drown, suffocate, actionEnabled, actionSound, attackEnabled, attackSound, attackDamage);
                plugin.getPlayerHandler().addEntityControlSettings(entityType, entityControlSettings);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Entity " + entityTypeString + " was not loaded.");
            }
        }
    }
}
