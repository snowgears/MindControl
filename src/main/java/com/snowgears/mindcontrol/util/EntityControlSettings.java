package com.snowgears.mindcontrol.util;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

public class EntityControlSettings {

    private EntityType entityType;
    private double speed;
    private boolean fly;
    private boolean drown;
    private boolean suffocate;
    private boolean actionEnabled;
    private Sound actionSound;
    private boolean attackEnabled;
    private Sound attackSound;
    private double attackDamage;

    public EntityControlSettings(EntityType entityType,
                                 double speed,
                                 boolean fly,
                                 boolean drown,
                                 boolean suffocate,
                                 boolean actionEnabled,
                                 Sound actionSound,
                                 boolean attackEnabled,
                                 Sound attackSound,
                                 double attackDamage){
        this.entityType = entityType;
        this.speed = speed;
        this.fly = fly;
        this.drown = drown;
        this.suffocate = suffocate;
        this.actionEnabled = actionEnabled;
        this.actionSound = actionSound;
        this.attackEnabled = attackEnabled;
        this.attackSound = attackSound;
        this.attackDamage = attackDamage;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isFly() {
        return fly;
    }

    public boolean isDrown() {
        return drown;
    }

    public boolean isSuffocate() {
        return suffocate;
    }

    public boolean isActionEnabled() {
        return actionEnabled;
    }

    public Sound getActionSound() {
        return actionSound;
    }

    public boolean isAttackEnabled() {
        return attackEnabled;
    }

    public Sound getAttackSound() {
        return attackSound;
    }

    public double getAttackDamage() {
        return attackDamage;
    }
}
