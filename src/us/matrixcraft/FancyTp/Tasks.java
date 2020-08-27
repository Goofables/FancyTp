package us.matrixcraft.FancyTp;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

class ExpTask implements Runnable {
    private final FancyTp plugin;
    private final Player player;
    private int cost;
    private int ticks;
    
    ExpTask(FancyTp plugin, Player player, int cost, int ticks) {
        this.player = player;
        this.plugin = plugin;
        this.cost = cost;
        this.ticks = ticks;
    }
    
    @Override
    public void run() {
        if (cost <= 0 || ticks <= 0) return;
        int xp = player.getTotalExperience();
        player.setExp(0.0f);
        player.setLevel(0);
        player.setTotalExperience(0);
        int amt = (int)Math.ceil(cost / ticks);
        player.giveExp(xp - amt);
        cost -= amt;
        ticks--;
        Bukkit.getScheduler().runTaskLater(plugin, this, 1); // Repeat?
    }
}

class TpTask implements Runnable {
    private final Player player;
    private final FancyTp plugin;
    private final Location from;
    private final Location to;
    
    TpTask(FancyTp plugin, Player player, Location from, Location to) {
        this.player = player;
        this.plugin = plugin;
        this.from = from;
        this.to = to;
    }
    
    @Override
    public void run() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.frozen.remove(player);
            to.getWorld().playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 2.0F, 1);
            to.getWorld().playSound(to, Sound.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 2.0F, 0.85f);
        }, 1);
        
        to.getWorld().spawnParticle(Particle.SMOKE_LARGE, from.clone().add(0, 1, 0), 500, 0, .25, 0, .1);
        from.getWorld().spawnParticle(Particle.END_ROD, to.clone().add(0, 1, 0), 500, 0, .5, 0, .25);
        //from.getWorld().strikeLightningEffect(from);
        //to.getWorld().strikeLightningEffect(to);
        
        from.getWorld().playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 2.0F, 1);
        from.getWorld().playSound(from, Sound.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 2.0F, 0.85f);
        from.getWorld().playSound(from, Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 2.0F, 0.0F);
        
        player.teleport(to, TeleportCause.UNKNOWN);
    }
}

class EffectTask implements Runnable {
    private final FancyTp plugin;
    private final Location from;
    private final Location to;
    private int time;
    
    EffectTask(FancyTp plugin, Location from, Location to, int time) {
        this.plugin = plugin;
        this.from = from;
        this.to = to;
        this.time = time;
    }
    
    @Override
    public void run() {
        from.getWorld().spawnParticle(Particle.PORTAL, from.clone().add(0, .5, 0), 1000, .1, .5, .1, 1);
        to.getWorld().spawnParticle(Particle.PORTAL, to.clone().add(0, .5, 0), 1000, .1, .5, .1, 1);
        time -= 1;
        if (time > 40) Bukkit.getScheduler().runTaskLater(plugin, this, 1L);
    }
}
