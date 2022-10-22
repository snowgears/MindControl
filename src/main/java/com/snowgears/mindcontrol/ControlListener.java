package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.event.PlayerMindControlAttemptEvent;
import com.snowgears.mindcontrol.event.PlayerMindControlReleaseEvent;
import com.snowgears.mindcontrol.util.*;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;


public class ControlListener implements Listener {

    public MindControl plugin = MindControl.getPlugin();

    private HashMap<UUID, Integer> timesSneaked = new HashMap<>();
    private HashMap<UUID, ProgressBar> playersStaringAtEntities = new HashMap<>();

    public ControlListener(MindControl instance) {
        plugin = instance;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onControlAttempt(PlayerMindControlAttemptEvent event){
        Player player = event.getPlayer();

        plugin.getLogger().log(Level.INFO, event.getPlayer().getName()+" attempted a mind control");
        if(event.getLivingEntity() != null) {
            plugin.getLogger().log(Level.INFO, event.getLivingEntity().getType().toString());
        }
        plugin.getLogger().log(Level.INFO, "AttemptState - "+event.getAttemptState().toString());

        if(event.getAttemptState() == AttemptState.FOCUS_START) {
            ProgressBar progressBar = new ProgressBar(player);
            playersStaringAtEntities.put(player.getUniqueId(), progressBar);
        }
        else if(event.getAttemptState() == AttemptState.FOCUS_END){
            ProgressBar progressBar = playersStaringAtEntities.get(player.getUniqueId());
            progressBar.destroy();
            playersStaringAtEntities.remove(player.getUniqueId());
        }
        else if(event.getAttemptState() == AttemptState.STARE_SUCCESS){
            plugin.getPlayerHandler().controlEntity(player, event.getLivingEntity());
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onControlRelease(PlayerMindControlReleaseEvent event){
        plugin.getLogger().log(Level.INFO, event.getPlayer().getName()+" released a mind control on "+event.getEntityData().getEntityType().toString()+". ReleaseReason - "+event.getReleaseReason().toString());

        plugin.getPlayerHandler().releaseEntity(event.getPlayer(), event.getReleaseReason());
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        try {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return; // off hand packet, ignore.
            }
        } catch (NoSuchMethodError error) {}
        final Player player = event.getPlayer();

        if(plugin.usePerms() && !player.hasPermission("mindcontrol.use")){
            ChatMessage.sendMessage(player, "error", "permUse", null, null);
            return;
        }

        //TODO come back to this
//        ItemStack helmet = player.getInventory().getHelmet();
//        if(helmet != null && helmet.getType() == (plugin.getMindControlHelmet().getType())) {
//            if (event.getRightClicked() instanceof LivingEntity) {
//                if(!plugin.isBlacklisted(event.getRightClicked().getType())) {
//                   //don't allow control if its a player and they have permission to not be controlled
//                    if(event.getRightClicked() instanceof Player){
//                        Player toControl = (Player)event.getRightClicked();
//                        if((!plugin.usePerms() && toControl.isOp()) || (plugin.usePerms() && player.hasPermission("mindcontrol.immune"))){
//                            player.sendMessage(ChatColor.RED+toControl.getName()+" is immune to mind control.");
//                            return;
//                        }
//                    }
//                    plugin.getPlayerHandler().releaseEntity(player, ReleaseReason.PLAYER_CHOICE);
//                    plugin.getPlayerHandler().controlEntity(player, (LivingEntity)event.getRightClicked());
//                }
//            }
//        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        plugin.getPlayerHandler().releaseEntity(event.getPlayer(), ReleaseReason.DISCONNECT);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event){
        System.out.println("Entity damaged. "+event.getEntity().getType().toString());
        if(event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            if (plugin.getPlayerHandler().isControllingEntity(player)) {
                if((player.getHealth() - event.getFinalDamage() <= 0)) {
                    event.setCancelled(true);
                    plugin.getPlayerHandler().releaseEntity(player, ReleaseReason.PLAYER_DEATH);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player){
            Player damager = (Player) event.getDamager();
            if (plugin.getPlayerHandler().isControllingEntity(damager)) {
                if(event.getEntity() instanceof Monster){
                    ((Monster) event.getEntity()).setTarget(damager);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        Player player = plugin.getPlayerHandler().getControllingPlayer(event.getEntity());
        if(player != null){
            event.getDrops().clear();
            event.setDroppedExp(0);
            plugin.getPlayerHandler().releaseEntity(player, ReleaseReason.FAKEPLAYER_DEATH);
            player.setHealth(0);
        }
        System.out.println("Entity death event. "+event.getEntity().getType().toString());
    }

    @EventHandler
    public void onCrouch(PlayerToggleSneakEvent event){
        final Player player = event.getPlayer();
        if(plugin.getPlayerHandler().isControllingEntity(player)) {
            if (event.isSneaking()) {
                Integer times = 0;
                if (timesSneaked.containsKey(player.getUniqueId())) {
                    times = timesSneaked.get(player.getUniqueId());
                }
                times++;

                if (times == 2) {

                    //call a new mind control release event
                    PlayerMindControlReleaseEvent releaseEvent = new PlayerMindControlReleaseEvent(player, plugin.getPlayerHandler().getControlledEntityData(player), ReleaseReason.PLAYER_CHOICE);
                    Bukkit.getPluginManager().callEvent(releaseEvent);

                    if (timesSneaked.containsKey(player.getUniqueId())) {
                        timesSneaked.remove(player.getUniqueId());
                    }
                }
                else
                    timesSneaked.put(player.getUniqueId(), times);

                MindControl.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MindControl.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        if (timesSneaked.containsKey(player.getUniqueId())) {
                            timesSneaked.remove(player.getUniqueId());
                        }
                    }
                }, 10L);
            }
        }
        else if(MindControlAPI.isWearingMindControlHelmet(player) && event.isSneaking()){

            PlayerMindControlAttemptEvent startFocusEvent = new PlayerMindControlAttemptEvent(player, null, AttemptState.FOCUS_START);
            Bukkit.getPluginManager().callEvent(startFocusEvent);
        }
        else{
            if(playersStaringAtEntities.containsKey(player.getUniqueId())){

                PlayerMindControlAttemptEvent startFocusEvent = new PlayerMindControlAttemptEvent(player, null, AttemptState.FOCUS_END);
                Bukkit.getPluginManager().callEvent(startFocusEvent);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onItemPickup(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        if(!canActAsPlayer(player))
            event.setCancelled(true);
    }

    //TODO come back to this
//    @EventHandler (priority = EventPriority.HIGHEST)
//    public void onMove(PlayerMoveEvent event){
//        if(plugin.getDistanceLimitSquared() != 0) {
//            Player player = event.getPlayer();
//            if (plugin.getPlayerHandler().isControllingEntity(player)) {
//                Location fakePlayerLoc = plugin.getPlayerHandler().getOldLocation(player);
//                if (fakePlayerLoc.distanceSquared(player.getLocation()) > plugin.getDistanceLimitSquared())
//                    plugin.getPlayerHandler().releaseEntity(player, ReleaseReason.DISTANCE_LIMIT);
//            }
//        }
//    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player)event.getEntity();
            if(!canActAsPlayer(player))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event){
        if(event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            UUID fakePlayerUUID = plugin.getPlayerHandler().getFakePlayer(player);
            if(fakePlayerUUID == null)
                return;

            if(event.getEntity().getLastDamageCause() != null && event.getEntity().getLastDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))
                return;

            boolean retargeted = false;
            if(fakePlayerUUID != null) {
                for (Entity entity : player.getWorld().getEntities()) {
                    if(entity.getUniqueId().equals(fakePlayerUUID)){
                        if(!entity.isInvulnerable()) {
                            if (entity.getLocation().distanceSquared(player.getLocation()) < 256) { //distance to player is less than 16 blocks
                                event.setTarget(entity);
                                retargeted = true;
                            }
                        }
                        break;
                    }
                }
            }
            // dont cancel event if the controlled entity is a player
            UUID controlledEntity = plugin.getPlayerHandler().getControlledEntityUUID(player);
            if(controlledEntity != null && Bukkit.getPlayer(controlledEntity) != null)
                return;
            //cancel event if it wasn't retargeted
            if(!retargeted)
                event.setCancelled(true);
        }
    }

    public boolean canActAsPlayer(Player player){
        if(plugin.getPlayerHandler().isControllingEntity(player)) {
            if(DisguiseAPI.isDisguised(player)){
                DisguiseType type = DisguiseAPI.getDisguise(player).getType();
                if(type == DisguiseType.PLAYER || type == DisguiseType.VILLAGER)
                    return true;
            }
            return false;
        }
        return true;
    }
}
