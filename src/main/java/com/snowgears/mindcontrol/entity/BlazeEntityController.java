package com.snowgears.mindcontrol.entity;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BlazeEntityController extends AbstractEntityController {

    public BlazeEntityController(Player player, LivingEntity livingEntity){
        super(player, livingEntity);
    }

    @Override
    public boolean doAttack(){
        boolean attack = super.doAttack();
        if(attack){
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setBounce(false);
            fireball.setYield((float) 1);
            fireball.setIsIncendiary(true);
            //fireball.setVelocity(new Vector(1, 1, 1));
            return true;
        }
        return false;
    }
}
