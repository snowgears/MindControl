package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.util.ChatMessage;
import com.snowgears.mindcontrol.util.ConfigUpdater;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MindControl extends JavaPlugin {

    private static MindControl plugin;
    private YamlConfiguration config;
    private final ControlListener controlListener = new ControlListener(this);
    private PlayerHandler playerHandler = new PlayerHandler(this);
    //private SpectatorHandler spectatorHandler = new SpectatorHandler();
    private RecipeLoader recipeLoader;
    private CommandHandler commandHandler;

    private boolean usePerms;
    private String commandAlias;

    public static MindControl getPlugin() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(controlListener, this);

        //define our data folder and create it
        File fileDirectory = new File(this.getDataFolder(), "Data");
        if (!fileDirectory.exists()) {
            boolean success;
            success = (fileDirectory.mkdirs());
            if (!success) {
                getServer().getConsoleSender().sendMessage("[MindControl]" + ChatColor.RED + " Data folder could not be created.");
            }
        }

        //load up the config file and make sure its updated with any new variables
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), configFile);
        }
        try {
            ConfigUpdater.update(plugin, "config.yml", configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        //load up the recipes file
        File recipeConfigFile = new File(getDataFolder(), "recipes.yml");
        if (!recipeConfigFile.exists()) {
            recipeConfigFile.getParentFile().mkdirs();
            this.copy(getResource("recipes.yml"), recipeConfigFile);
        }
        recipeLoader = new RecipeLoader(plugin);

        //load the chatConfig file
        File chatConfigFile = new File(getDataFolder(), "chatConfig.yml");
        if (!chatConfigFile.exists()) {
            chatConfigFile.getParentFile().mkdirs();
            copy(getResource("chatConfig.yml"), chatConfigFile);
        }

        usePerms = config.getBoolean("usePermissions");
        commandAlias = config.getString("command");

        new ChatMessage(this);
        commandHandler = new CommandHandler(this, "mindcontrol.operator", commandAlias, "Base command for the Mind Control plugin", "/control", new ArrayList(Arrays.asList(commandAlias)));
    }

    public void onDisable(){
        recipeLoader.unloadRecipes();
    }

    public void reload(){
        HandlerList.unregisterAll(controlListener);

        onDisable();
        onEnable();
    }

    public boolean usePerms(){
        return usePerms;
    }

    public String getCommandAlias() {
        return commandAlias;
    }

    public PlayerHandler getPlayerHandler(){
        return playerHandler;
    }

//    public SpectatorHandler getSpectatorHandler(){
//        return spectatorHandler;
//    }

    public void spawnLine(Location location, Location target, Particle particle, int particleCount) {
        int step = 0;
        int particles = 100;
        double amount = particles / 10;
        Vector link = target.toVector().subtract(location.toVector());
        float length = (float) link.length();
        link.normalize();

        float ratio = length / particles;
        Vector v = link.multiply(ratio);
        Location loc = location.clone().subtract(v);
        for (int i = 0; i < particles; i++) {
            if (step >= amount) {
                step = 0;
            }
            step++;
            loc = loc.add(v);
            location.getWorld().spawnParticle(particle, loc, particleCount);
        }
    }

    public void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}