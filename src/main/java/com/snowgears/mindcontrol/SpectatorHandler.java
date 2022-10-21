package com.snowgears.mindcontrol;

import com.snowgears.mindcontrol.util.ReflectionUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpectatorHandler {

    private Class<?> camera;
    //key: UUID of player controlling
    //value: UUID of player spectating
    private HashMap<UUID, UUID> spectatingPlayers;

    //key: UUID of player spectating
    //value: the old game mode of that player
    private HashMap<UUID, GameMode> oldGameModes;

    public SpectatorHandler(){
        this.camera = ReflectionUtil.getNMSClass("PacketPlayOutCamera");

        spectatingPlayers = new HashMap<>();
        oldGameModes = new HashMap<>();
    }

    public void setCamera(Player p, Entity ent) {
        setCamera(p, ent.getEntityId());
        spectatingPlayers.put(ent.getUniqueId(), p.getUniqueId());
    }

    public void removeCamera(Player p) {
        setCamera(p, p.getEntityId());
        if(oldGameModes.containsKey(p.getUniqueId())) {
            GameMode originalGameMode = oldGameModes.get(p.getUniqueId());
            p.setGameMode(originalGameMode);
        }
        for(Map.Entry<UUID, UUID> entry : spectatingPlayers.entrySet()){
            if(entry.getValue().equals(p.getUniqueId()))
                spectatingPlayers.remove(entry.getKey());
        }
    }

    private void setCamera(final Player p, final int entityID) {
        oldGameModes.put(p.getUniqueId(), p.getGameMode());
        p.setGameMode(GameMode.SPECTATOR);

        MindControl.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MindControl.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    Object packet = camera.newInstance();
                    Field field = packet.getClass().getDeclaredField("a");
                    field.setAccessible(true);
                    field.set(packet, entityID);
                    sendPacket(p, packet);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 5L);
    }

    private void sendPacket(Player p, Object packet) {
        ReflectionUtil.sendPacketSync(p, new Object[] { packet });
    }

//    private Class<?> getNMSClass(String className) {
//        String fullName = "net.minecraft.server." + getVersion() +"."+ className; //should be v1_10_R1
//        Class<?> clazz = null;
//        try {
//            clazz = Class.forName(fullName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return clazz;
//    }
//
//    private Object getNMSPlayer(Player p) {
//        try {
//            return p.getClass().getMethod("getHandle").invoke(p);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public String getVersion() {
//        String pkg = Bukkit.getServer().getClass().getPackage().getName();
//        return pkg.substring(pkg.lastIndexOf(".") + 1);
//    }
}
