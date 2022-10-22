package com.snowgears.mindcontrol.entity;


import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * This class takes an Entity and stores info about where it is.
 *
 */

public class EntityData {

    private LivingEntity entity;
    private EntityType entityType;
    private Location oldLocation;
    private UUID uuid;
    private Location releaseLocation;

    private boolean hasAI;
    private boolean isInvisible;
    private boolean isInvulnerable;

    private Entity fakeControllingPlayer;
    //private Location fakeControllingPlayerLocation;
    //private UUID fakeControllingPlayerUUID;

    public EntityData(LivingEntity entity) {
        this.entity = entity;
        this.entityType = entity.getType();
        this.oldLocation = entity.getLocation().clone();
        this.uuid = entity.getUniqueId();

        this.hasAI = entity.hasAI();
        this.isInvisible = entity.isInvisible();
        this.isInvulnerable = entity.isInvulnerable();
    }

    private void setFakeControllingPlayer(Entity fakeControllingPlayer){
        this.fakeControllingPlayer = fakeControllingPlayer;
        //this.fakeControllingPlayerLocation = fakeControllingPlayer.getLocation();
        //this.fakeControllingPlayerUUID = fakeControllingPlayer.getUniqueId();
    }

    public void capture(Entity fakeControllingPlayer){
        setFakeControllingPlayer(fakeControllingPlayer);

        entity.setAI(false);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        Location hidingLocation = entity.getLocation().clone();
        hidingLocation.setY(2);
        entity.teleport(hidingLocation);
    }

    public void setReleaseLocation(Location location){
        this.releaseLocation = location;
    }

    public LivingEntity release(){

        if(fakeControllingPlayer != null){
            fakeControllingPlayer.remove();
            System.out.println("Removed fake player (pig)");
        }
//        else {
//            if (fakeControllingPlayerLocation != null) {
//                for (Entity fakePlayer : fakeControllingPlayerLocation.getWorld().getChunkAt(fakeControllingPlayerLocation).getEntities()) {
//                    if (fakePlayer.getUniqueId().equals(fakeControllingPlayerUUID)) {
//                        fakePlayer.remove();
//                        System.out.println("Removed fake player (pig) by UUID");
//                    }
//                }
//            }
//        }

        if(entity != null && !entity.isDead()){
            if (releaseLocation != null) {
                entity.teleport(releaseLocation);

                entity.setAI(hasAI);
                entity.setInvisible(isInvisible);
                entity.setInvulnerable(isInvulnerable);

                System.out.println("Teleported entity to release location - " + releaseLocation.getX() + ", " + releaseLocation.getY() + ", " + releaseLocation.getZ());
            } else {
                entity.teleport(oldLocation);
                System.out.println("Teleported entity to old location - " + oldLocation.getX() + ", " + oldLocation.getY() + ", " + oldLocation.getZ());
            }
            return entity;
        }
//        else {
//            System.out.println("ENTITY NOT FOUND OR IT WAS DEAD ALREADY");
//            for (Entity entity : oldLocation.getWorld().getChunkAt(oldLocation).getEntities()) {
//                if (entity.getUniqueId().equals(uuid)) {
//                    entity.setVelocity(new Vector(0, 0, 0));
//                    entity.setFallDistance(0);
//                    if (releaseLocation != null) {
//                        entity.teleport(releaseLocation);
//                        System.out.println("UUID Teleported entity to release location - " + releaseLocation.getX() + ", " + releaseLocation.getY() + ", " + releaseLocation.getZ());
//                    } else {
//                        entity.teleport(oldLocation);
//                        System.out.println("UUID Teleported entity to old location - " + oldLocation.getX() + ", " + oldLocation.getY() + ", " + oldLocation.getZ());
//                    }
//                    return (LivingEntity) entity;
//                }
//            }
//        }
        System.out.println("Not found. Returned null.");
        return null;
    }

    public Location getOldLocation(){
        return oldLocation;
    }

    public EntityType getEntityType(){
        return entityType;
    }

    public UUID getUniqueID() {
        return uuid;
    }
}
