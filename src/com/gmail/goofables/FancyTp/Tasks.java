package com.gmail.goofables.FancyTp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

class ExpTask implements Runnable {
   private Player player = null;
   private int cost = 0;
   private int ticks = 0;
   private FancyTp plugin;
   
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
      player.setExp(0);
      player.setLevel(0);
      player.setTotalExperience(0);
      int amt = (int)Math.ceil(cost / ticks);
      player.giveExp(xp - amt);
      cost -= amt;
      ticks--;
      Bukkit.getScheduler().runTaskLater(this.plugin, this, 1); // Repeat?
   }
}

class TpTask implements Runnable {
   private Player player;
   private FancyTp plugin;
   private Location from, to;
   
   TpTask(FancyTp plugin, Player player, Location from, Location to) {
      this.player = player;
      this.plugin = plugin;
      this.from = from;
      this.to = to;
   }
   
   @Override
   public void run() {
      Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.frozen.remove(player), 5);
      
      to.getWorld().spawnParticle(Particle.SMOKE_LARGE, from.clone().add(0, .5, 0), 500, 0, .25, 0, .1);
      from.getWorld().spawnParticle(Particle.END_ROD, to.clone().add(0, .5, 0), 500, 0, .5, 0, .25);
      from.getWorld().strikeLightningEffect(from);
      to.getWorld().strikeLightningEffect(to);
      player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
   }
}

class EffectTask implements Runnable {
   private FancyTp plugin;
   private Location from, to;
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
      if (time > 40) Bukkit.getScheduler().runTaskLater(plugin, this, 1);
   }
}
