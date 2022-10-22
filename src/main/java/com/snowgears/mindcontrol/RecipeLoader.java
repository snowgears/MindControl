package com.snowgears.mindcontrol;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.snowgears.mindcontrol.util.HelmetSettings;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class RecipeLoader {

    private MindControl plugin;
    private File recipesFile;

    public RecipeLoader(MindControl plugin){
        this.plugin = plugin;
        this.recipesFile = new File(plugin.getDataFolder(), "recipes.yml");

        loadRecipes();
    }

    private void loadRecipes() {
        int loadedCount = 0;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipesFile);
        if (config.getConfigurationSection("recipes") == null) {
            plugin.getLogger().log(Level.SEVERE, "Recipes file could not be loaded.");
            return;
        }

        Set<String> allRecipeNumbers = config.getConfigurationSection("recipes").getKeys(false);

        for (String recipeNumber : allRecipeNumbers) {
            boolean enabled = config.getBoolean("recipes." + recipeNumber + ".enabled");
            if (enabled) {
                String id = config.getString("recipes." + recipeNumber + ".id");
                String name = config.getString("recipes." + recipeNumber + ".name");
                List<String> lore;
                try {
                    lore = config.getStringList("recipes." + recipeNumber + ".lore");
                } catch (NullPointerException e) {
                    lore = new ArrayList<>();
                }
                String headTextureID = config.getString("recipes." + recipeNumber + ".headTextureID");
                int uses = config.getInt("recipes." + recipeNumber + ".uses");
                double captureTime = config.getDouble("recipes." + recipeNumber + ".captureTime");
                int distanceLimit = config.getInt("recipes." + recipeNumber + ".distanceLimit");
                int timeLimit = config.getInt("recipes." + recipeNumber + ".timeLimit");
                int timeBetweenUses = config.getInt("recipes." + recipeNumber + ".timeBetweenUses");

                String stareSoundString = config.getString("recipes." + recipeNumber + ".sound.stare");
                Sound stareSound = null;
                try {
                    stareSound = Sound.valueOf(stareSoundString);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "unrecognized sound in recipe "+recipeNumber+": "+stareSoundString);
                }
                String controlSoundString = config.getString("recipes." + recipeNumber + ".sound.control");
                Sound controlSound = null;
                try {
                    controlSound = Sound.valueOf(controlSoundString);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "unrecognized sound in recipe "+recipeNumber+": "+controlSoundString);
                }

                String progressBarColorString = config.getString("recipes." + recipeNumber + ".progressBar.color");
                BarColor progressBarColor;
                try {
                    progressBarColor = BarColor.valueOf(progressBarColorString);
                } catch (IllegalArgumentException e) {
                    progressBarColor = BarColor.PURPLE;
                }
                String progressBarStyleString = config.getString("recipes." + recipeNumber + ".progressBar.style");
                BarStyle progressBarStyle;
                try {
                    progressBarStyle = BarStyle.valueOf(progressBarStyleString);
                } catch (IllegalArgumentException e) {
                    progressBarStyle = BarStyle.SOLID;
                }

                HelmetSettings helmetSettings = new HelmetSettings(id, uses, captureTime, distanceLimit, timeLimit, timeBetweenUses, stareSound, controlSound, progressBarColor, progressBarStyle);

                try {
                    boolean isBlackList = false;
                    String blackListString = config.getString("recipes." + recipeNumber + ".entity.listType");
                    List<String> entityTypeStrings = config.getStringList("recipes." + recipeNumber + ".entity.list");

                    List<EntityType> entityTypeList = new ArrayList<>();
                    for(String entityTypeString : entityTypeStrings){
                        try {
                            if(!entityTypeString.isEmpty())
                                entityTypeList.add(EntityType.valueOf(entityTypeString));
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "unrecognized entity type in recipe "+recipeNumber+": "+entityTypeString);
                        }
                    }

                    if(blackListString.equalsIgnoreCase("blacklist"))
                        isBlackList = true;
                    helmetSettings.setEntityList(isBlackList, entityTypeList);
                } catch (NullPointerException e){
                    helmetSettings.setEntityList(true, new ArrayList<>());
                }

                ItemStack helmetItem = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta helmetItemMeta = (SkullMeta) helmetItem.getItemMeta();
                helmetItemMeta.setDisplayName(formatString(name, uses));
                if(lore != null && !lore.isEmpty()) {
                    List<String> loreList = new ArrayList<>();
                    for(String loreString : lore) {
                        loreList.add(formatString(loreString, uses));
                    }
                    helmetItemMeta.setLore(loreList);
                }

                GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                profile.getProperties().put("textures", new Property("texture", headTextureID));
                try {
                    Field profileField = helmetItemMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(helmetItemMeta, profile);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }

                PersistentDataContainer persistentData = helmetItemMeta.getPersistentDataContainer();
                persistentData.set(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, uses);
                persistentData.set(new NamespacedKey(plugin, "id"), PersistentDataType.STRING, id);

                helmetItem.setItemMeta(helmetItemMeta);

                helmetSettings.setHelmetItem(helmetItem);
                //register the helmet setting in map
                plugin.getPlayerHandler().addHelmetSettings(id, helmetSettings);

                NamespacedKey key = new NamespacedKey(plugin, "mind_helmet_" + id);
                ShapedRecipe recipe = new ShapedRecipe(key, helmetItem);

                HashMap<String, Material> materialMap = new HashMap<>();
                Set<String> materialKeys = config.getConfigurationSection("recipes." + recipeNumber + ".recipe.materials").getKeys(false);
                for (String materialKey : materialKeys) {
                    try {
                        Material material = Material.valueOf(config.getString("recipes." + recipeNumber + ".recipe.materials." + materialKey));
                        materialMap.put(materialKey, material);
                    } catch (IllegalArgumentException iae) {
                        plugin.getLogger().log(Level.WARNING, "ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                    } catch (NullPointerException npe) {
                        plugin.getLogger().log(Level.WARNING, "NULL ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                    }
                }

                //System.out.println("recipe #"+recipeNumber);
                String[] threeLettersArray = new String[3];
                List<String> recipeLines = config.getStringList("recipes." + recipeNumber + ".recipe.shape");
                for (int i = 0; i < recipeLines.size(); i++) {
                    String threeLetters = "";
                    String[] splitRecipeLine = recipeLines.get(i).split("\\[");
                    for (String splitRecipeLinePart : splitRecipeLine) {
                        if (splitRecipeLinePart.contains("]")) {
                            String letter = splitRecipeLinePart.replaceAll("]", "");
                            if (letter.isEmpty())
                                letter = "_";
                            threeLetters += letter;
                        }
                    }
                    threeLettersArray[i] = threeLetters;
                }

                if(threeLettersArray[0].startsWith("_") && threeLettersArray[1].startsWith("_") && threeLettersArray[2].startsWith("_")){
                    threeLettersArray[0] = threeLettersArray[0].substring(1);
                    threeLettersArray[1] = threeLettersArray[1].substring(1);
                    threeLettersArray[2] = threeLettersArray[2].substring(1);
                }
                if(threeLettersArray[0].endsWith("_") && threeLettersArray[1].endsWith("_") && threeLettersArray[2].endsWith("_")){
                    threeLettersArray[0] = threeLettersArray[0].substring(0, threeLettersArray[0].length()-1);
                    threeLettersArray[1] = threeLettersArray[0].substring(0, threeLettersArray[1].length()-1);
                    threeLettersArray[2] = threeLettersArray[0].substring(0, threeLettersArray[2].length()-1);
                }



                if(onlyContainsUnderscores(threeLettersArray[0])){
                    recipe.shape(threeLettersArray[1], threeLettersArray[2]);
                    if(threeLettersArray[1].contains("_") || threeLettersArray[2].contains("_")) {
                        materialMap.put("_", Material.AIR);
                    }
                }
                else if(onlyContainsUnderscores(threeLettersArray[2])){
                    recipe.shape(threeLettersArray[0], threeLettersArray[1]);
                    if(threeLettersArray[0].contains("_") || threeLettersArray[1].contains("_")) {
                        materialMap.put("_", Material.AIR);
                    }
                }
                else {
                    try {
                        recipe.shape(threeLettersArray[0], threeLettersArray[1], threeLettersArray[2]);

                        if(threeLettersArray[0].contains("_") || (threeLettersArray[1].contains("_") || threeLettersArray[2].contains("_"))) {
                            materialMap.put("_", Material.AIR);
                        }

                    } catch (IllegalArgumentException e){
                        plugin.getLogger().log(Level.WARNING, "Problem with shaping recipe "+recipeNumber);
                    }
                }
                //System.out.println(threeLettersArray[0] + ", "+ threeLettersArray[1] + ", "+threeLettersArray[2]);
                //recipe.shape("1 1")

                for (Map.Entry<String, Material> entry : materialMap.entrySet()) {
                    if(entry.getValue().toString().contains("_PLANKS")){
                        recipe.setIngredient(entry.getKey().charAt(0), new RecipeChoice.MaterialChoice(Tag.PLANKS));
                    }
                    else {
                        try {
                            recipe.setIngredient(entry.getKey().charAt(0), entry.getValue());
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "Problem with setting ingredient in recipe "+recipeNumber);
                        }
                    }
                }
                Bukkit.addRecipe(recipe);
                loadedCount++;
            }
        }

        plugin.getLogger().log(Level.INFO, "Loaded "+loadedCount+" recipes.");
    }

    private boolean onlyContainsUnderscores(String s){
        for (int i =0; i< s.length(); i++){
            char c = s.charAt(i);
            if(c != '_'){
                return false;
            }
        }
        return true;
    }

    public void unloadRecipes(){
        NamespacedKey key;
        for(String helmetID : plugin.getPlayerHandler().getHelmetIDs()){
            key = new NamespacedKey(plugin, "mind_helmet_" + helmetID);
            Bukkit.removeRecipe(key);
        }
    }

    private String formatString(String unformattedString, int uses){
        unformattedString = unformattedString.replace("[uses]", ""+uses);
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        return unformattedString;
    }
}
