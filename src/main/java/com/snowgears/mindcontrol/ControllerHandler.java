package com.snowgears.mindcontrol;


import com.snowgears.mindcontrol.entity.AbstractEntityController;
import com.snowgears.mindcontrol.entity.EntityData;
import com.snowgears.mindcontrol.entity.PlayerData;
import com.snowgears.mindcontrol.util.EntityControlSettings;
import com.snowgears.mindcontrol.util.HelmetSettings;
import com.snowgears.mindcontrol.util.ReleaseReason;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class ControllerHandler {

    private MindControl plugin;

    private HashMap<String, HelmetSettings> helmetSettingsMap = new HashMap<>();
    private HashMap<EntityType, EntityControlSettings> entityControlSettingsMap = new HashMap<>();

    //KEY: UUID of player controlling the entity
    //VALUE: ControllerData
    //   - PlayerData: The saved previous state of the player controlling the entity
    //   - EntityData: The saved previous state of the entity that is being controlled
    private HashMap<UUID, AbstractEntityController> controlMap = new HashMap<>();

    public ControllerHandler(MindControl plugin){
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

    public boolean controlEntity(final Player player, LivingEntity entity) {
        if (controlMap.containsKey(player.getUniqueId()))
            return false;

        AbstractEntityController abstractEntityController = AbstractEntityController.create(player, entity);
        controlMap.put(player.getUniqueId(), abstractEntityController);
        return true;
    }

    public boolean releaseEntity(Player player, ReleaseReason reason) {
        if(!controlMap.containsKey(player.getUniqueId()))
            return false;

        AbstractEntityController abstractEntityController = controlMap.get(player.getUniqueId());
        abstractEntityController.releaseEntity(player, reason);
        controlMap.remove(player.getUniqueId());
        return true;
    }

//    public void controlEntity(final Player player, LivingEntity entity){
//        if(controlMap.containsKey(player.getUniqueId()))
//            return;
//
//        Sound stareSound = MindControlAPI.getStareSound(player.getInventory().getHelmet());
//        Sound controlSound = MindControlAPI.getControlSound(player.getInventory().getHelmet());
//
//        //save current states of both the player and the entity to be controlled
//        PlayerData playerData = new PlayerData(player);
//        EntityData entityData = new EntityData(entity);
//
//        //construct a player disguise that will be put on the fake player
//        Disguise disguise = DisguiseAPI.constructDisguise(entity);
//        PlayerDisguise playerDisguise = new PlayerDisguise(player.getName());
//
//        PlayerWatcher fw = playerDisguise.getWatcher();
//        fw.setArmor(player.getInventory().getArmorContents());
//        playerDisguise.setWatcher(fw); //TODO for some reason this doesn't put the players armor on the disguise
//
//        //spawn the fake player and put on their disguise
//        LivingEntity fakePlayer = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.PIG);
//
//        for(Attribute attribute : Attribute.values()) {
//
//            //fake player will get all real player's attributes
//            AttributeInstance fakePlayerAttributeInstance = fakePlayer.getAttribute(attribute);
//            if(fakePlayerAttributeInstance != null) {
//                AttributeInstance playerAttributeInstance = player.getAttribute(attribute);
//                if(playerAttributeInstance != null) {
//                    fakePlayerAttributeInstance.setBaseValue(playerAttributeInstance.getValue());
//                }
//            }
//
//            //real player will get all entity's attributes
//            AttributeInstance playerAttributeInstance = player.getAttribute(attribute);
//            if(playerAttributeInstance != null) {
//                AttributeInstance entityAttributeInstance = entity.getAttribute(attribute);
//                if(entityAttributeInstance != null) {
//                    playerAttributeInstance.setBaseValue(entityAttributeInstance.getValue());
//                }
//            }
//        }
//
//        fakePlayer.setHealth(player.getHealth());
//        fakePlayer.setRemainingAir(player.getRemainingAir());
//        fakePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5));
//        fakePlayer.setSilent(true);
//        fakePlayer.setAI(false);
//        if (player.getGameMode() == GameMode.CREATIVE)
//            fakePlayer.setInvulnerable(true);
//        DisguiseAPI.disguiseToAll(fakePlayer, playerDisguise);
//        //TODO in future make it so fakePlayer can pickup items that get returned to real player
//
//        //add the fake player uuid to the player data (so the fake player can be removed later)
//        playerData.setFakePlayerUUID(fakePlayer.getUniqueId());
//        //put all of this data into the control map
//        EntityController controllerData = new EntityController(playerData, entityData);
//        controlMap.put(player.getUniqueId(), controllerData);
//
//        //if the player is attempting to control another player
//        if(entity instanceof Player){
//            Player toControl = (Player)entity;
//            // give controlling player the same data as controlled player
//            PlayerData toControlData = new PlayerData(toControl);
//            toControlData.apply(player);
//
//            // force controlled player to watch through controlling player's eyes
//            //plugin.getSpectatorHandler().setCamera(toControl, player);
//            toControl.setGameMode(GameMode.SPECTATOR);
//            toControl.setSpectatorTarget(player);
//            //TODO set variables in config: viewSelfDisguisePlayer, viewSelfDisguiseMob
//            //disguise.setViewSelfDisguise(false); //dont want to see other players disguise on self. Interferes too much with block placement and other things
//            disguise.setSelfDisguiseVisible(false);
//            //TODO make simple method to check if disguise is of a player or not for other listener methods
//        }
//        else {
//            //TODO all this code should be handled in PlayerData?
//            player.setFoodLevel(20); //set full hunger bar
//            player.setRemainingAir(entity.getRemainingAir()); //set health of player to the entity they are controlling
//            //player.setMaxHealth(entity.getMaxHealth()); //match max health
//            AttributeInstance playerHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
//            playerHealthAttribute.setBaseValue(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
//            player.setHealth(entity.getHealth()); //match actual health
//            player.setFireTicks(entity.getFireTicks());
//            player.setGameMode(GameMode.ADVENTURE);
//            player.getInventory().clear();
//
//            //TODO just test code
//            if(entity.getType() == EntityType.BLAZE){
//                player.setAllowFlight(true);
//                player.setFlying(true);
//                //player.setFlySpeed(1);
//            }
//
//            entityData.capture(fakePlayer);
//            player.teleport(entityData.getOldLocation());
//        }
//        DisguiseAPI.disguiseToAll(player, disguise);
//
//        if(stareSound != null) {
//            player.stopSound(stareSound);
//        }
//        if(controlSound != null) {
//            player.playSound(entityData.getOldLocation(), controlSound, 1, 1);
//        }
//
//        //running this packet on a timer while player is inside of another entity should prevent them from sprinting
////        PacketPlayOutUpdateHealth test;
////        test=new PacketPlayOutUpdateHealth((float)player.getHealth(), 2, player.getSaturation());
////        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(test);
//
//        //TODO this will be deleted later. Just for testing now
//        player.sendMessage(ChatColor.GRAY + "You take control of the "+ disguise.getType().toString().toLowerCase() +" and begin to see through its eyes.");
//
//        //TODO come back to this code later. This makes particle beams
//        //set up repeating tasks for particle line and experience bar timer
////        if(plugin.getUseParticles()) {
////            final Location fakePlayerLoc = fakePlayer.getLocation().clone().add(0,1.8,0);
////            int taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
////                @Override
////                public void run() {
////                    plugin.spawnLine(fakePlayerLoc, player.getLocation().clone().add(0, 1, 0));
////                }
////            }, 0L, 10L);
////
////            playerData.setBeamTaskID(taskID);
////            controllerData.setPlayer(playerData);
////            controlMap.put(player.getUniqueId(), controllerData);
////        }
////        if(plugin.getTimeLimit() > 0){
////            player.setLevel(plugin.getTimeLimit());
////            int taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
////                @Override
////                public void run() {
////                    player.setLevel(player.getLevel()-1);
////                    if(player.getLevel() == 0)
////                        releaseEntity(player, ReleaseReason.TIME_LIMIT);
////                }
////            }, 20L, 20L);
////
////            playerData.setTimerTaskID(taskID);
////            controllerData.setPlayer(playerData);
////            controlMap.put(player.getUniqueId(), controllerData);
////        }
//    }
//
//    public void releaseEntity(Player player, ReleaseReason reason){
//        if(!controlMap.containsKey(player.getUniqueId()))
//            return;
//
//        EntityController controllerData = controlMap.get(player.getUniqueId());
//        PlayerData playerData = controllerData.getPlayer();
//        EntityData entityData = controllerData.getEntity();
//        entityData.setReleaseLocation(player.getLocation().clone().add(0, 0.1, 0));
//
//        Disguise disguise = DisguiseAPI.getDisguise(player);
//        EntityType type = EntityType.valueOf(disguise.getType().toString());
//
//        LivingEntity livingEntity;
//        if(type != EntityType.PLAYER)
//            livingEntity = entityData.release();
//        else
//            livingEntity = Bukkit.getPlayer(entityData.getUniqueID());
//
//        if(livingEntity != null){
//            //give all player data to the living entity
//            PlayerData newData = new PlayerData(player);
//            newData.apply(livingEntity);
//
//            if(reason == ReleaseReason.PLAYER_DEATH)
//                livingEntity.setHealth(0);
//        }
//
//        switch(reason){
//            case PLAYER_DEATH:
//                ChatMessage.sendMessage(player, "info", "entityDeath", player.getInventory().getHelmet(), player.getName());
//                break;
//            case FAKEPLAYER_DEATH:
//                player.setHealth(0);
//                //TODO save playerData to file here as well, then also when player respawn event check the file
//                ChatMessage.sendMessage(player, "info", "playerDeath", player.getInventory().getHelmet(), player.getName());
//                break;
//            case DISTANCE_LIMIT:
//                ChatMessage.sendMessage(player, "info", "distanceLimit", player.getInventory().getHelmet(), player.getName());
//                break;
//            case TIME_LIMIT:
//                ChatMessage.sendMessage(player, "info", "timeLimit", player.getInventory().getHelmet(), player.getName());
//                break;
//            case PLAYER_CHOICE:
//                ChatMessage.sendMessage(player, "info", "closeConnection", player.getInventory().getHelmet(), player.getName());
//                break;
//            case DISCONNECT:
//                //TODO save playerData to file instead of returning
//                //TODO *OR* in returnData method, save to file if the player is null, then just return on login
//                break;
//        }
//
//        playerData.apply(player);
//
//        //TODO this will be moved to PlayerData. Just a test line
//        if(type == EntityType.BLAZE){
//            player.setAllowFlight(false);
//            player.setFlying(false);
//            //player.setFlySpeed(1);
//        }
//
//        //TODO apply fakePlayer data to real player (health, fireticks, air, new items, etc...)
//        DisguiseAPI.undisguiseToAll(player);
//        controlMap.remove(player.getUniqueId());
//    }

    public Location getOldLocation(Player player){
        if(controlMap.containsKey(player.getUniqueId())){
            AbstractEntityController controllerData = controlMap.get(player.getUniqueId());
            return controllerData.getPlayer().getOldLocation();
        }
        return null;
    }

    public UUID getControlledEntityUUID(Player player){
        if(controlMap.containsKey(player.getUniqueId())){
            AbstractEntityController controllerData = controlMap.get(player.getUniqueId());
            return controllerData.getEntity().getUniqueID();
        }
        return null;
    }

    public EntityData getControlledEntityData(Player player){
        if(controlMap.containsKey(player.getUniqueId())){
            AbstractEntityController controllerData = controlMap.get(player.getUniqueId());
            return controllerData.getEntity();
        }
        return null;
    }

    public Player getControllingPlayer(LivingEntity fakePlayer){
        for(Map.Entry<UUID, AbstractEntityController> entry : controlMap.entrySet()) {
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

    public void addEntityControlSettings(EntityType entityType, EntityControlSettings entityControlSettings){
        this.entityControlSettingsMap.put(entityType, entityControlSettings);
    }

    public EntityControlSettings getEntityControlSettings(EntityType entityType){
        if(entityControlSettingsMap.containsKey(entityType))
            return entityControlSettingsMap.get(entityType);
        return null;
    }

    public AbstractEntityController getEntityController(Player player){
        return controlMap.get(player.getUniqueId());
    }
}
