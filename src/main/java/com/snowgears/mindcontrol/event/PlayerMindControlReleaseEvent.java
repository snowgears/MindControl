package com.snowgears.mindcontrol.event;

import com.snowgears.mindcontrol.entity.EntityData;
import com.snowgears.mindcontrol.util.ReleaseReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMindControlReleaseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private EntityData entityData;
    private ReleaseReason releaseReason;
    private boolean cancelled;

    public PlayerMindControlReleaseEvent(Player p, EntityData e, ReleaseReason r) {
        player = p;
        entityData = e;
        releaseReason = r;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public ReleaseReason getReleaseReason(){
        return releaseReason;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean set) {
        cancelled = set;
    }
}