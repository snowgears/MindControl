package com.snowgears.mindcontrol.entity;

import org.bukkit.entity.Entity;

public abstract class EntityExtraData implements Cloneable {
    public abstract EntityExtraData clone();
    public abstract void apply(Entity entity);
}
