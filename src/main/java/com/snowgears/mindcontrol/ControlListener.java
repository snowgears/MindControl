package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.util.ChatMessage;
import com.snowgears.mindcontrol.util.HelmetSettings;
import com.snowgears.mindcontrol.util.ReleaseReason;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class ControlListener implements Listener {

    public MindControl plugin = MindControl.getPlugin();

    private HashMap<UUID, Integer> timesSneaked = new HashMap<>();

    public ControlListener(MindControl instance) {
        plugin = instance;
    }

    //this method calls PlayerCreateShopEvent
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
    public void onDeath(EntityDamageEvent event){
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
                    plugin.getPlayerHandler().releaseEntity(event.getPlayer(), ReleaseReason.PLAYER_CHOICE);
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
            UUID controlledEntity = plugin.getPlayerHandler().getControlledEntity(player);
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
