package com.snowgears.mindcontrol.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GenericEntityController extends AbstractEntityController {

    public GenericEntityController(Player player, LivingEntity livingEntity){
        super(player, livingEntity);
    }
}
