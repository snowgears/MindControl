package com.snowgears.mindcontrol;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MindControl extends JavaPlugin {

    private static MindControl plugin;
    private YamlConfiguration config;
    private final ControlListener controlListener = new ControlListener(this);
    private PlayerHandler playerHandler = new PlayerHandler(this);
    private SpectatorHandler spectatorHandler = new SpectatorHandler();

    private boolean usePerms;
    private ItemStack mindControlHelmet;
    private boolean useParticles;
    private Particle particle;
    private int particleCount = 1;
    private int timeLimit;
    private int distanceLimitSquared;
    private List<EntityType> entityBlacklist = new ArrayList<EntityType>();

    public static MindControl getPlugin() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(controlListener, this);

        File fileDirectory = new File(this.getDataFolder(), "Data");
        if (!fileDirectory.exists()) {
            boolean success;
            success = (fileDirectory.mkdirs());
            if (!success) {
                getServer().getConsoleSender().sendMessage("[MindControl]" + ChatColor.RED + " Data folder could not be created.");
            }
        }

        File helmetItemFile = new File(fileDirectory, "helmetItem.yml");
        if(helmetItemFile.exists()){
            YamlConfiguration currencyConfig = YamlConfiguration.loadConfiguration(helmetItemFile);
            mindControlHelmet = currencyConfig.getItemStack("item");
            mindControlHelmet.setAmount(1);
        }
        else{
            try {
                mindControlHelmet = new ItemStack(Material.GOLDEN_HELMET);
                helmetItemFile.createNewFile();

                YamlConfiguration currencyConfig = YamlConfiguration.loadConfiguration(helmetItemFile);
                currencyConfig.set("item", mindControlHelmet);
                currencyConfig.save(helmetItemFile);
            } catch (Exception e) {}
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), configFile);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        usePerms = config.getBoolean("usePermissions");
        useParticles = config.getBoolean("showParticleBeam");

        String sParticle = config.getString("particleEffect");
        try{
            particle = Particle.valueOf(sParticle);
        } catch(Exception e) {
            particle = Particle.ENCHANTMENT_TABLE;
        }
        particleCount = config.getInt("particleCount");
        timeLimit = config.getInt("timeLimit");
        distanceLimitSquared = config.getInt("distanceLimit");
        distanceLimitSquared *= distanceLimitSquared;

        for(String s : config.getStringList("entityBlacklist")){
            try{
                entityBlacklist.add(EntityType.valueOf(s));
            } catch (Exception e) {}
        }
    }

    public void onDisable() {
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("control") && args.length == 0){
//            //TODO implement command for taking control of Players
//            if(sender instanceof Player){
//                Player player = (Player)sender;
//                for(Entity entity : player.getNearbyEntities(5,2,5)){
//                    if(entity instanceof LivingEntity){
//                        this.getPlayerHandler().setCamera(player, entity);
//                        return true;
//                    }
//                }
//            }
        }
        return true;
    }

    public boolean usePerms(){
        return usePerms;
    }

    public ItemStack getMindControlHelmet(){
        return mindControlHelmet;
    }

    public boolean getUseParticles(){
        return useParticles;
    }

    public PlayerHandler getPlayerHandler(){
        return playerHandler;
    }

    public SpectatorHandler getSpectatorHandler(){
        return spectatorHandler;
    }

    public int getTimeLimit(){
        return timeLimit;
    }

    public int getDistanceLimitSquared(){
        return distanceLimitSquared;
    }

    public boolean isBlacklisted(EntityType entityType){
        return entityBlacklist.contains(entityType);
    }

    public void spawnLine(Location location, Location target) {
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

    public void setMindControlHelmet(ItemStack helmet){
        this.mindControlHelmet = helmet;

        try {
            File fileDirectory = new File(getDataFolder(), "Data");
            File helmetItemFile = new File(fileDirectory, "helmetItem.yml");
            YamlConfiguration currencyConfig = YamlConfiguration.loadConfiguration(helmetItemFile);
            currencyConfig.set("item", plugin.getMindControlHelmet());
            currencyConfig.save(helmetItemFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}