package com.snowgears.mindcontrol.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMindControlActionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private LivingEntity livingEntity;
    private boolean cancelled;

    //TODO this event will be called when a player does an action as another entity
    //like blows up a creeper, plays a sound effect, etc

    public PlayerMindControlActionEvent(Player p, LivingEntity e) {
        player = p;
        livingEntity = e;
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