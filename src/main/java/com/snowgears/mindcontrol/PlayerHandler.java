package com.snowgears.mindcontrol;


import com.snowgears.mindcontrol.entity.ControllerData;
import com.snowgears.mindcontrol.entity.EntityData;
import com.snowgears.mindcontrol.entity.PlayerData;
import com.snowgears.mindcontrol.util.ChatMessage;
import com.snowgears.mindcontrol.util.HelmetSettings;
import com.snowgears.mindcontrol.util.ReleaseReason;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerHandler {

    private MindControl plugin;

    private HashMap<String, HelmetSettings> helmetSettingsMap = new HashMap<>();

    //KEY: UUID of player controlling the entity
    //VALUE: ControllerData
    //   - PlayerData: The saved previous state of the player controlling the entity
    //   - EntityData: The saved previous state of the entity that is being controlled
    private HashMap<UUID, ControllerData> controlMap = new HashMap<>();

    public PlayerHandler(MindControl plugin){
        this.plugin = plugin;
    }

    //TODO give all mobs (except players) their own spawn eggs (that can't be dropped or used) that do special abilities
    //prevent sprinting from all of these and maybe make walk speed slightly slower
    //all special ability items will also play the mob sound in the world
    //bat - can fly
    //chicken - can slow fall
    //cow - nothing
    //mooshroom - creates patch of mushrooms around it
    //pig - pick up/kick off passenger
    //rabbit - nothing
    //sheep - nothing
    //horse/donkey/mule/skeleton-horse - pick up/kick off passenger
    //squid - breath underwater + swim faster
    //villager(non-zombie) - same actions as player (place blocks)
    //cave spider/spider - climb walls (check if your wallclimb plugin still works in 1.10)
    //enderman - teleport
    //polar bear - drop fish on ground if standing in water (cooldown on this)
    //zombie pigman - gold sword that doesn't break
    //creeper - explode
    //gaurdian - shoot stuff?
    //endermite - nothing?
    //husk/zombie - burn in sunlight
    //magma cube/slime - cut down size + spawn other cubes around it
    //shulker - no ideas yet
    //silverfish - nothing?
    //skeleton/stray/wither skeleton - bow with infinite arrows (cooldown on shots?)
    //witch - throw potions
    //ocelot - jump 3 blocks high
    //wolf - run faster?
    //iron golem - super strength + knockback
    //snow golem - coat ground in snow + throw snowballs

    //blaze - fly and shoot fireballs (cooldown + delay?)
    //ghast - fly and shoot fireballs (cooldown + delay?)
    //ender dragon - fly + blow fireballs (cooldown)
    //wither skeleton - fly and shoot fireballs (cooldown + delay)





    public boolean isControllingEntity(Player player){
        return controlMap.containsKey(player.getUniqueId());
    }

    public void controlEntity(final Player player, LivingEntity entity){
        if(controlMap.containsKey(player.getUniqueId()))
            return;

        //save current states of both the player and the entity to be controlled
        PlayerData playerData = new PlayerData(player);
        EntityData entityData = new EntityData(entity);

        //construct a player disguise that will be put on the fake player
        Disguise disguise = DisguiseAPI.constructDisguise(entity);
        PlayerDisguise playerDisguise = new PlayerDisguise(player.getName());
        PlayerWatcher fw = playerDisguise.getWatcher();
        fw.setArmor(player.getInventory().getArmorContents());
        playerDisguise.setWatcher(fw); //TODO for some reason this doesn't put the players armor on the disguise

        //spawn the fake player and put on their disguise
        LivingEntity fakePlayer = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.PIG);
        fakePlayer.setMaxHealth(player.getMaxHealth());
        fakePlayer.setHealth(player.getHealth());
        fakePlayer.setRemainingAir(player.getRemainingAir());
        fakePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5));
        fakePlayer.setSilent(true);
        if (player.getGameMode() == GameMode.CREATIVE)
            fakePlayer.setInvulnerable(true);
        DisguiseAPI.disguiseToAll(fakePlayer, playerDisguise);
        //TODO in future make it so fakePlayer can pickup items that get returned to real player

        //add the fake player uuid to the player data (so the fake player can be removed later)
        playerData.setFakePlayerUUID(fakePlayer.getUniqueId());
        //put all of this data into the control map
        ControllerData controllerData = new ControllerData(playerData, entityData);
        controlMap.put(player.getUniqueId(), controllerData);

        //if the player is attempting to control another player
        if(entity instanceof Player){
            Player toControl = (Player)entity;
            // give controlling player the same data as controlled player
            PlayerData toControlData = new PlayerData(toControl);
            toControlData.apply(player);

            // force controlled player to watch through controlling player's eyes
            //plugin.getSpectatorHandler().setCamera(toControl, player);
            //TODO set variables in config: viewSelfDisguisePlayer, viewSelfDisguiseMob
            disguise.setViewSelfDisguise(false); //dont want to see other players disguise on self. Interferes too much with block placement and other things
            //TODO make simple method to check if disguise is of a player or not for other listener methods
        }
        else {
            player.setFoodLevel(20); //set full hunger bar
            player.setRemainingAir(entity.getRemainingAir()); //set health of player to the entity they are controlling
            player.setMaxHealth(entity.getMaxHealth()); //match max health
            player.setHealth(entity.getHealth()); //match actual health
            player.setFireTicks(entity.getFireTicks());
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();

            player.teleport(entity.getLocation());
            entity.remove();
        }
        DisguiseAPI.disguiseToAll(player, disguise);

        //TODO this will be deleted later. Just for testing now
        player.sendMessage(ChatColor.GRAY + "You take control of the "+ disguise.getType().toString().toLowerCase() +" and begin to see through its eyes.");

        //TODO come back to this code later. This makes particle beams
        //set up repeating tasks for particle line and experience bar timer
//        if(plugin.getUseParticles()) {
//            final Location fakePlayerLoc = fakePlayer.getLocation().clone().add(0,1.8,0);
//            int taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
//                @Override
//                public void run() {
//                    plugin.spawnLine(fakePlayerLoc, player.getLocation().clone().add(0, 1, 0));
//                }
//            }, 0L, 10L);
//
//            playerData.setBeamTaskID(taskID);
//            controllerData.setPlayer(playerData);
//            controlMap.put(player.getUniqueId(), controllerData);
//        }
//        if(plugin.getTimeLimit() > 0){
//            player.setLevel(plugin.getTimeLimit());
//            int taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
//                @Override
//                public void run() {
//                    player.setLevel(player.getLevel()-1);
//                    if(player.getLevel() == 0)
//                        releaseEntity(player, ReleaseReason.TIME_LIMIT);
//                }
//            }, 20L, 20L);
//
//            playerData.setTimerTaskID(taskID);
//            controllerData.setPlayer(playerData);
//            controlMap.put(player.getUniqueId(), controllerData);
//        }
    }

    public void releaseEntity(Player player, ReleaseReason reason){
        if(!controlMap.containsKey(player.getUniqueId()))
            return;

        ControllerData controllerData = controlMap.get(player.getUniqueId());
        PlayerData playerData = controllerData.getPlayer();
        EntityData entityData = controllerData.getEntity();
        entityData.setLocation(player.getLocation().clone().add(0, 0.5, 0));

        for(Entity e : playerData.getOldLocation().getWorld().getEntities()){
            if(e.getUniqueId().equals(playerData.getFakePlayerUUID())){
                e.remove();
            }
        }

        Disguise disguise = DisguiseAPI.getDisguise(player);
        EntityType type = EntityType.valueOf(disguise.getType().toString());

        LivingEntity livingEntity;
        if(type != EntityType.PLAYER)
            livingEntity = (LivingEntity) entityData.spawn();
        else
            livingEntity = Bukkit.getPlayer(entityData.getUniqueID());

        if(livingEntity != null){
            //give all player data to the living entity
            PlayerData newData = new PlayerData(player);
            newData.apply(livingEntity);

            if(reason == ReleaseReason.PLAYER_DEATH)
                livingEntity.setHealth(0);

        }

        switch(reason){
            case PLAYER_DEATH:
                ChatMessage.sendMessage(player, "info", "entityDeath", player.getInventory().getHelmet(), player.getName());
                break;
            case FAKEPLAYER_DEATH:
                player.setHealth(0);
                //TODO save playerData to file here as well, then also when player respawn event check the file
                ChatMessage.sendMessage(player, "info", "playerDeath", player.getInventory().getHelmet(), player.getName());
                break;
            case DISTANCE_LIMIT:
                ChatMessage.sendMessage(player, "info", "distanceLimit", player.getInventory().getHelmet(), player.getName());
                break;
            case TIME_LIMIT:
                ChatMessage.sendMessage(player, "info", "timeLimit", player.getInventory().getHelmet(), player.getName());
                break;
            case PLAYER_CHOICE:
                ChatMessage.sendMessage(player, "info", "closeConnection", player.getInventory().getHelmet(), player.getName());
                break;
            case DISCONNECT:
                //TODO save playerData to file instead of returning
                //TODO *OR* in returnData method, save to file if the player is null, then just return on login
                break;
        }

        playerData.apply(player);
        //TODO apply fakePlayer data to real player (health, fireticks, air, new items, etc...)
        DisguiseAPI.undisguiseToAll(player);
        controlMap.remove(player.getUniqueId());
    }

    public Location getOldLocation(Player player){
        if(controlMap.containsKey(player.getUniqueId())){
            ControllerData controllerData = controlMap.get(player.getUniqueId());
            return controllerData.getPlayer().getOldLocation();
        }
        return null;
    }

    public UUID getControlledEntityUUID(Player player){
        if(controlMap.containsKey(player.getUniqueId())){
            ControllerData controllerData = controlMap.get(player.getUniqueId());
            return controllerData.getEntity().getUniqueID();
        }
        return null;
    }

    public EntityData getControlledEntityData(Player player){
        if(controlMap.containsKey(player.getUniqueId())){
            ControllerData controllerData = controlMap.get(player.getUniqueId());
            return controllerData.getEntity();
        }
        return null;
    }

    public Player getControllingPlayer(LivingEntity fakePlayer){
        for(Map.Entry<UUID, ControllerData> entry : controlMap.entrySet()) {
            PlayerData playerData = entry.getValue().getPlayer();
            if (fakePlayer.getUniqueId().equals(playerData.getFakePlayerUUID())) {
                return plugin.getServer().getPlayer(playerData.getUUID());
            }
        }
        return null;
    }

    public UUID getFakePlayer(Player realPlayer){
        if(isControllingEntity(realPlayer)){
            PlayerData playerData = controlMap.get(realPlayer.getUniqueId()).getPlayer();
            return playerData.getFakePlayerUUID();
        }
        return null;
    }

    public HelmetSettings getHelmetSettings(String id){
        if(helmetSettingsMap.containsKey(id))
            return helmetSettingsMap.get(id);
        return null;
    }

    public void addHelmetSettings(String id, HelmetSettings helmetSettings){
        this.helmetSettingsMap.put(id, helmetSettings);
    }

    public List<String> getHelmetIDs(){
        ArrayList<String> helmetIDs = new ArrayList<>();
        for(String id : helmetSettingsMap.keySet()){
            helmetIDs.add(id);
        }
        Collections.sort(helmetIDs);
        return helmetIDs;
    }
}
