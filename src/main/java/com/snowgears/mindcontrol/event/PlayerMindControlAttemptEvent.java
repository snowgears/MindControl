package com.snowgears.mindcontrol.event;

import com.snowgears.mindcontrol.util.AttemptState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMindControlAttemptEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private LivingEntity livingEntity;
    private AttemptState attemptState;
    private boolean cancelled;

    public PlayerMindControlAttemptEvent(Player p, LivingEntity e, AttemptState state) {
        player = p;
        livingEntity = e;
        attemptState = state;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public AttemptState getAttemptState(){
        return attemptState;
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