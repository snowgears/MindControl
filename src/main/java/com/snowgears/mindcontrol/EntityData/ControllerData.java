package com.snowgears.mindcontrol.EntityData;

public class ControllerData {

    private PlayerData playerData;
    private EntityData entityData;

    public ControllerData(PlayerData playerData, EntityData entityData){
        this.playerData = playerData;
        this.entityData = entityData;
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
}
