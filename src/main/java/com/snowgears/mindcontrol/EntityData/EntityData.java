package com.snowgears.mindcontrol.EntityData;


import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This class stores information about an Entity.
 *
 */
//TODO clean up how all entity data is stored and returned
    //TODO things like fireticks, air level, location, should be initalization through a Player
    //TODO spawn(PlayerData data) -> will spawn entity with all normal attributes except use things like health and location from PlayerData

public class EntityData implements Cloneable {
    protected static Map<UUID, WeakReference<Entity>> respawned = new HashMap<UUID, WeakReference<Entity>>();

    protected WeakReference<Entity> entity = null;
    protected UUID uuid = null;
    protected EntityExtraData extraData;
    protected Location location;
    protected String name = null;
    protected EntityType type;
    protected BlockFace facing;
    protected ItemStack item;
    protected double health = 1;
    protected boolean isBaby;
    protected int fireTicks;
    protected int airLevel;
    protected DyeColor dyeColor;
    protected SkeletonType skeletonType;
    protected Ocelot.Type ocelotType;
    protected Villager.Profession villagerProfession;
    protected Collection<PotionEffect> potionEffects = null;
    protected boolean hasPotionEffects = false;
    protected boolean isLiving = false;
    protected boolean isProjectile = false;

    public EntityData(Entity entity) {
        this(entity.getLocation(), entity);
    }

    private EntityData(Location location, Entity entity) {
        setEntity(entity);
        this.isLiving = entity instanceof LivingEntity;
        this.isProjectile = entity instanceof Projectile;
        this.type = entity.getType();
        this.location = location;
        this.fireTicks = entity.getFireTicks();

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            name = li.getCustomName();
            this.health = li.getHealth();
            this.potionEffects = li.getActivePotionEffects();
            this.airLevel = li.getRemainingAir();
        }

        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            this.isBaby = !ageable.isAdult();
        }

        if (entity instanceof Colorable) {
            Colorable colorable = (Colorable)entity;
            dyeColor = colorable.getColor();
        }


        if(entity instanceof Player) {
            extraData = new PlayerData((Player)entity);
        }
        if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            item = droppedItem.getItemStack();
        } else if (entity instanceof Horse) {
            extraData = new EntityHorseData((Horse)entity);
        } else if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton)entity;
            skeletonType = skeleton.getSkeletonType();
        } else if (entity instanceof Villager) {
            Villager villager = (Villager)entity;
            villagerProfession = villager.getProfession();
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            dyeColor = wolf.getCollarColor();
        } else if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot) entity;
            ocelotType = ocelot.getCatType();
        }
    }

    private EntityData(EntityType type) {
        this.type = type;
    }

    public void setEntity(Entity entity) {
        this.entity = entity == null ? null : new WeakReference<Entity>(entity);
        this.uuid = entity == null ? null : entity.getUniqueId();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location){
        this.location = location;
    }

    public EntityType getType() {
        return type;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getHealth() {
        return health;
    }

    protected Entity trySpawn() {
        Entity spawned = null;
        try {
            switch (type) {
                case PLAYER:
                    // Nope!
                    break;
                case DROPPED_ITEM:
                    spawned = location.getWorld().dropItem(location, item);
                    break;
                default:
                    spawned = location.getWorld().spawnEntity(location, type);
            }
        } catch (Exception ex) {
            org.bukkit.Bukkit.getLogger().log(Level.WARNING, "[MindControl] Error restoring entity type " + getType() + " at " + getLocation(), ex);
        }
        return spawned;
    }

    public Entity spawn() {
        if (location == null) return null;
        Entity spawned = trySpawn();
        if (spawned != null) {
            modify(spawned);
        }

        return spawned;
    }

    //TODO add a PlayerData (or LivingEntity) parameter to modify that will use things like updated health and location
    public boolean modify(Entity entity) {
        if (entity == null || entity.getType() != type || !entity.isValid()) return false;

        if (extraData != null) {
            extraData.apply(entity);
        }

        entity.setFireTicks(fireTicks);
        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            if (isBaby) {
                ageable.setBaby();
            } else {
                ageable.setAdult();
            }
        }

        if (entity instanceof Colorable) {
            Colorable colorable = (Colorable)entity;
            colorable.setColor(dyeColor);
        }

        if (entity instanceof Painting) {
            Painting painting = (Painting) entity;
            painting.setFacingDirection(facing, true);
        }
        else if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            itemFrame.setItem(item);
            itemFrame.setFacingDirection(facing, true);
        } else if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            droppedItem.setItemStack(item);
        } else if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton)entity;
            skeleton.setSkeletonType(skeletonType);
        } else if (entity instanceof Villager) {
            Villager villager = (Villager)entity;
            villager.setProfession(villagerProfession);
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            wolf.setCollarColor(dyeColor);
        } else if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)entity;
            ocelot.setCatType(ocelotType);
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity) entity;
            if (hasPotionEffects) {
                Collection<PotionEffect> currentEffects = li.getActivePotionEffects();
                for (PotionEffect effect : currentEffects) {
                    li.removePotionEffect(effect.getType());
                }
                if (potionEffects != null) {
                    for (PotionEffect effect : potionEffects) {
                        li.addPotionEffect(effect);
                    }
                }
            }

            if (name != null && name.length() > 0) {
                li.setCustomName(name);
            }

            try {
                li.setHealth(Math.min(health, li.getMaxHealth()));
                li.setRemainingAir(Math.min(airLevel, li.getRemainingAir()));
            } catch (Throwable ex) {
            }
        }
        return true;
    }

    public void setHasPotionEffects(boolean changed) {
        this.hasPotionEffects = changed;
    }

    public Entity getEntity() {
        return entity == null ? null : entity.get();
    }

    public UUID getUniqueID(){
        return uuid;
    }

    public void applyExtraData(Entity entity){
        extraData.apply(entity);
    }

    public EntityData clone() {
        try {
            return (EntityData)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
