package com.snowgears.mindcontrol.util;

import com.snowgears.mindcontrol.MindControl;
import com.snowgears.mindcontrol.event.PlayerMindControlAttemptEvent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

public class ProgressBar {

    private Player player;
    private double totalSeconds;
    private int updateInterval;
    private long startTime;

    private BukkitTask task;
    private BossBar bossBar;
    private LivingEntity entityStaringAt;

    private AttemptState attemptState;

    public ProgressBar(Player player){
        this.player = player;
        this.totalSeconds = MindControlAPI.getCaptureTime(player.getInventory().getHelmet());
        this.updateInterval = 3;

        createBossBar(player);

        startStaringTask();
    }

    private void startStaringTask(){

        task = new BukkitRunnable() {
            public void run() {

                long currentTime = System.currentTimeMillis();
                float secondsPassed = (currentTime - startTime) / 1000F;

                //only do this if player has already been staring at something
                if(attemptState == AttemptState.STARE_START) {
                    if (secondsPassed >= totalSeconds) {
                        setAttemptState(AttemptState.STARE_SUCCESS);
                        return;
                    }
                }

                int distanceLimit = MindControlAPI.getDistanceLimit(player);

                //check if they are looking at a LivingEntity
                RayTraceResult rayResult = Bukkit.getWorld("world").rayTraceEntities(
                        player.getEyeLocation().add(player.getLocation().getDirection()), player.getEyeLocation().getDirection(), distanceLimit, entity -> !entity.getUniqueId().equals(player.getUniqueId())
                );
                if(rayResult == null){
                    setAttemptState(AttemptState.STARE_FAIL);
                    return;
                }

                Entity hitEntity = rayResult.getHitEntity();
                if(hitEntity == null || hitEntity.isDead() || !(hitEntity instanceof LivingEntity)){
                    setAttemptState(AttemptState.STARE_FAIL);
                    return;
                }
                if(entityStaringAt != null && !entityStaringAt.getUniqueId().equals(hitEntity.getUniqueId())){
                    setAttemptState(AttemptState.STARE_FAIL);
                    return;
                }
                if(entityStaringAt == null){
                    setAttemptState(AttemptState.STARE_START);
                }
                entityStaringAt = (LivingEntity) hitEntity;
                setTitleEntityFocus();

                double percentage = secondsPassed / totalSeconds;
                if(percentage > 1)
                    percentage = 1;
                else if(percentage < 0)
                    percentage = 0;
                bossBar.setProgress(percentage);
                //System.out.println(secondsPassed + " / "+totalSeconds+" = "+percentage);
            }
        }.runTaskTimer(MindControl.getPlugin(), 0, updateInterval);
    }

    public void destroy(){
        if(bossBar != null) {
            bossBar.setVisible(false);
        }

        if(task != null){
            task.cancel();
        }
    }

    private void createBossBar(Player player){
        String title = ChatMessage.getMessage("progressBar", "noEntity", player.getInventory().getHelmet(), player.getName());
        bossBar =  Bukkit.createBossBar(title, MindControl.getPlugin().getProgressBarColor(), MindControl.getPlugin().getProgressBarStyle());
        bossBar.setProgress(0);
        bossBar.addPlayer(player);
        startTime = Long.MAX_VALUE;
    }

    private void setAttemptState(AttemptState attemptState) {

        switch (attemptState){
            case STARE_START:
                startTime = System.currentTimeMillis();
                bossBar.setProgress(0);
                bossBar.setVisible(true);

                break;
            case STARE_FAIL:
                if(this.attemptState != null && this.attemptState != attemptState){
                    PlayerMindControlAttemptEvent attemptEvent = new PlayerMindControlAttemptEvent(player, entityStaringAt, attemptState);
                    Bukkit.getPluginManager().callEvent(attemptEvent);
                }
                startTime = Long.MAX_VALUE;
                entityStaringAt = null;
                String title = ChatMessage.getMessage("progressBar", "noEntity", player.getInventory().getHelmet(), player.getName());
                bossBar.setTitle(title);
                bossBar.setProgress(0);

                break;
            case STARE_SUCCESS:
                if(this.attemptState == AttemptState.STARE_START){
                    PlayerMindControlAttemptEvent attemptEvent = new PlayerMindControlAttemptEvent(player, entityStaringAt, attemptState);
                    Bukkit.getPluginManager().callEvent(attemptEvent);
                }
                destroy();

                break;
            default:
                break;
        }
        this.attemptState = attemptState;
    }

    private void setTitleEntityFocus() {
        if (entityStaringAt != null) {
            String title = ChatMessage.getMessage("progressBar", "entity", player.getInventory().getHelmet(), player.getName());

            title = title.replace("[entity_type]", entityStaringAt.getType().toString().toLowerCase());
            if (title != null && !title.isEmpty()) {
                bossBar.setTitle(title);
            }
        }
    }
}
