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
   
   ExpTask(Player player, int cost, int ticks) {
      this.player = player;
      this.cost = cost;
      this.ticks = ticks;
   }
   
   @Override
   public void run() {
      if (cost <= 0 || ticks <= 0) {
         Thread.currentThread().interrupt();
         return;
      }
      int xp = player.getTotalExperience();
      player.setExp(0);
      player.setLevel(0);
      player.setTotalExperience(0);
      int amt = (int)Math.ceil(cost / ticks);
      player.giveExp(xp - amt);
      cost -= amt;
      ticks--;
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
      
      from.getWorld().spawnParticle(Particle.END_ROD, from.clone().add(0, .5, 0), 500, 0, .5, 0, .25);
      to.getWorld().spawnParticle(Particle.SMOKE_LARGE, to.clone().add(0, .5, 0), 500, 0, .25, 0, .1);
      from.getWorld().strikeLightningEffect(from);
      to.getWorld().strikeLightningEffect(to);
      player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
   }
}
