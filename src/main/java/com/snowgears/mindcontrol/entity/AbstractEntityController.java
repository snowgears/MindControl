package com.snowgears.mindcontrol.entity;

import com.snowgears.mindcontrol.MindControl;
import com.snowgears.mindcontrol.util.ChatMessage;
import com.snowgears.mindcontrol.util.EntityControlSettings;
import com.snowgears.mindcontrol.util.MindControlAPI;
import com.snowgears.mindcontrol.util.ReleaseReason;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class AbstractEntityController {

    protected Player player;
    protected LivingEntity livingEntity;
    protected PlayerData playerData;
    protected EntityData entityData;

    protected EntityControlSettings entityControlSettings;

    public static AbstractEntityController create(Player player, LivingEntity livingEntity) {

        switch(livingEntity.getType()){
            case BLAZE:
                return new BlazeEntityController(player, livingEntity);
            case CREEPER:
                return new CreeperEntityController(player, livingEntity);
            default:
                return new GenericEntityController(player, livingEntity);
        }
    }

    public AbstractEntityController(Player player, LivingEntity livingEntity){
        this.player = player;
        this.livingEntity = livingEntity;

        this.playerData = new PlayerData(player);
        this.entityData = new EntityData(livingEntity);
        this.entityControlSettings = MindControl.getPlugin().getPlayerHandler().getEntityControlSettings(livingEntity.getType());

        controlEntity(player, livingEntity);
    }

    public boolean doAction(){
        if(entityControlSettings != null && entityControlSettings.isActionEnabled()){
            Sound actionSound = entityControlSettings.getActionSound();
            if(player != null && actionSound != null){
                player.getWorld().playSound(player.getLocation(), actionSound, 1, 1);
                return true;
            }
        }
        return false;
    }

    public boolean doAttack(){
        if(entityControlSettings != null && entityControlSettings.isAttackEnabled()){
            Sound attackSound = entityControlSettings.getAttackSound();
            if(player != null && attackSound != null){
                player.getWorld().playSound(player.getLocation(), attackSound, 1, 1);
                return true;
            }
        }
        return false;
    }

    // The saved previous state of the player controlling the entity
    public PlayerData getPlayer(){
        return playerData;
    }

    // The saved previous state of the entity that is being controlled
    public EntityData getEntity(){
        return entityData;
    }

    public void setPlayer(PlayerData playerData){
        this.playerData = playerData;
    }

    public void setEntity(EntityData entityData){
        this.entityData = entityData;
    }

    private void controlEntity(final Player player, LivingEntity entity){
        Sound stareSound = MindControlAPI.getStareSound(player.getInventory().getHelmet());
        Sound controlSound = MindControlAPI.getControlSound(player.getInventory().getHelmet());

        //construct a player disguise that will be put on the fake player
        Disguise disguise = DisguiseAPI.constructDisguise(entity);
        PlayerDisguise playerDisguise = new PlayerDisguise(player.getName());

        PlayerWatcher fw = playerDisguise.getWatcher();
        fw.setArmor(player.getInventory().getArmorContents());
        playerDisguise.setWatcher(fw); //TODO for some reason this doesn't put the players armor on the disguise

        //spawn the fake player and put on their disguise
        LivingEntity fakePlayer = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.PIG);

        for(Attribute attribute : Attribute.values()) {

            //fake player will get all real player's attributes
            AttributeInstance fakePlayerAttributeInstance = fakePlayer.getAttribute(attribute);
            if (fakePlayerAttributeInstance != null) {
                AttributeInstance playerAttributeInstance = player.getAttribute(attribute);
                if (playerAttributeInstance != null) {
                    fakePlayerAttributeInstance.setBaseValue(playerAttributeInstance.getValue());
                }
            }
        }
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

        if(entityControlSettings != null) {
            AttributeInstance playerSpeedInstance = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (playerSpeedInstance != null) {
                playerSpeedInstance.setBaseValue(entityControlSettings.getSpeed());
            }

            AttributeInstance playerAttackDamageInstance = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (playerAttackDamageInstance != null) {
                playerAttackDamageInstance.setBaseValue(entityControlSettings.getAttackDamage());
            }

            if(entityControlSettings.isFly()){
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setFlySpeed((float)entityControlSettings.getSpeed());
            }
            else{
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }




        fakePlayer.setHealth(playerData.getHealth());
        fakePlayer.setRemainingAir(player.getRemainingAir());
        fakePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5));
        fakePlayer.setSilent(true);
        fakePlayer.setAI(false);
        if (player.getGameMode() == GameMode.CREATIVE)
            fakePlayer.setInvulnerable(true);
        DisguiseAPI.disguiseToAll(fakePlayer, playerDisguise);
        //TODO in future make it so fakePlayer can pickup items that get returned to real player

        //add the fake player uuid to the player data (so the fake player can be removed later)
        playerData.setFakePlayerUUID(fakePlayer.getUniqueId());
        //put all of this data into the control map
//        EntityController controllerData = new EntityController(playerData, entityData);
//        controlMap.put(player.getUniqueId(), controllerData);

        //if the player is attempting to control another player
        if(entity instanceof Player){
            Player toControl = (Player)entity;
            // give controlling player the same data as controlled player
            PlayerData toControlData = new PlayerData(toControl);
            toControlData.apply(player);

            // force controlled player to watch through controlling player's eyes
            //plugin.getSpectatorHandler().setCamera(toControl, player);
            toControl.setGameMode(GameMode.SPECTATOR);
            toControl.setSpectatorTarget(player);
            //TODO set variables in config: viewSelfDisguisePlayer, viewSelfDisguiseMob
            //disguise.setViewSelfDisguise(false); //dont want to see other players disguise on self. Interferes too much with block placement and other things
            disguise.setSelfDisguiseVisible(false);
            //TODO make simple method to check if disguise is of a player or not for other listener methods
        }
        else {
            //TODO all this code should be handled in PlayerData?
            player.setFoodLevel(20); //set full hunger bar
            player.setRemainingAir(entity.getRemainingAir()); //set health of player to the entity they are controlling
            //player.setMaxHealth(entity.getMaxHealth()); //match max health
            AttributeInstance playerHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            playerHealthAttribute.setBaseValue(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setHealth(entity.getHealth()); //match actual health
            player.setFireTicks(entity.getFireTicks());
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();

            //TODO just test code
            if(entity.getType() == EntityType.BLAZE){
                player.setAllowFlight(true);
                player.setFlying(true);
                //player.setFlySpeed(1);
            }

            entityData.capture(fakePlayer);
            player.teleport(entityData.getOldLocation());
        }
        DisguiseAPI.disguiseToAll(player, disguise);

        if(stareSound != null) {
            player.stopSound(stareSound);
        }
        if(controlSound != null) {
            player.playSound(entityData.getOldLocation(), controlSound, 1, 1);
        }

        //running this packet on a timer while player is inside of another entity should prevent them from sprinting
//        PacketPlayOutUpdateHealth test;
//        test=new PacketPlayOutUpdateHealth((float)player.getHealth(), 2, player.getSaturation());
//        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(test);

        //TODO this will be deleted later. Just for testing now
        player.sendMessage(ChatColor.GRAY + "You take control of the "+ disguise.getType().toString().toLowerCase() +" and begin to see through its eyes.");

    }

    public void releaseEntity(Player player, ReleaseReason reason){

        entityData.setReleaseLocation(player.getLocation().clone().add(0, 0.1, 0));

        Disguise disguise = DisguiseAPI.getDisguise(player);
        EntityType type = EntityType.valueOf(disguise.getType().toString());

        LivingEntity livingEntity;
        if(type != EntityType.PLAYER)
            livingEntity = entityData.release();
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

        //TODO this will be moved to PlayerData. Just a test line
        if(type == EntityType.BLAZE){
            player.setAllowFlight(false);
            player.setFlying(false);
            //player.setFlySpeed(1);
        }

        //TODO apply fakePlayer data to real player (health, fireticks, air, new items, etc...)
        DisguiseAPI.undisguiseToAll(player);
    }
}
