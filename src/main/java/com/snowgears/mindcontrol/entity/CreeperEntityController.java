package com.snowgears.mindcontrol.entity;

import com.snowgears.mindcontrol.MindControl;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CreeperEntityController extends AbstractEntityController {

    private boolean attacked;

    public CreeperEntityController(Player player, LivingEntity livingEntity){
        super(player, livingEntity);
    }

    @Override
    public boolean doAttack(){
        super.doAttack();
        if(!attacked) {
            attacked = true;
            super.doAttack();
            Disguise disguise = DisguiseAPI.getDisguise(player);
            CreeperWatcher creeperWatcher = (CreeperWatcher) disguise.getWatcher();
            creeperWatcher.setIgnited(true);

            MindControl.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MindControl.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if(DisguiseAPI.getDisguise(player) != null) {
                        player.setHealth(0.5);
                        player.getWorld().createExplosion(player.getLocation(), 3, false, true, livingEntity);
                    }
                }
            }, 30L); //1.5 seconds

            return true;
        }
        return false;
    }
}
