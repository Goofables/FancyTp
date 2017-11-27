package com.gmail.goofables;

/**
 * Spigot Template created by ***REMOVED*** on {DATE}.
 * <p>
 * //TODO:
 */

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
   private FileConfiguration config = getConfig();
   private List<String> disabled;
   private ArrayList<Player> frozen = new ArrayList<>();
   private ConfigurationSection messages = config.getConfigurationSection("messages");
   
   @Override
   public void onEnable() {
      config.addDefault("exclude", new String[] {"MyPlayerName123"});
      disabled = config.getStringList("exclude");
      if (!config.isConfigurationSection("messages")) config.createSection("messages");
      config.addDefault("messages.title", "&aWarping");
      config.addDefault("messages.subtitle", "&cCost: &6%cost%");
      messages = config.getConfigurationSection("messages");
      config.options().copyDefaults(true);
      saveConfig();
      getServer().getPluginManager().registerEvents(this, this);
   }
   
   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent e) {
      //System.out.println("Player: " + e.getPlayer() + " From: " + e.getFrom() + " To: " + e.getTo() + " Cause: " + e.getCause());
      //for (RegisteredListener rl : e.getHandlers().getRegisteredListeners())
      //   System.out.println(" -> Pl:" + rl.getPlugin().getName() + " RL: " + rl.getListener());
      final Player player = e.getPlayer();
      final Location from = e.getFrom().clone();
      final Location to = e.getTo().clone();
      if (disabled.contains(player.getName())) return;
      if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
         if (VanishAPI.isInvisible(player)) return;
      }
      if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
         Essentials ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
         User u = ess.getUser(player);
         if (u != null && u.isHidden()) return;
      }
      if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) return;
      
      e.setCancelled(true);
      int expCost = (int)from.distance(to);
      int exp = player.getTotalExperience();
      if (expCost > exp) {
         player.sendMessage("§cNot enough exp! Required: §6" + expCost + "§c Currently Have: §6" + exp + "§c!");
         return;
      } else {
         player.setExp(0);
         player.setLevel(0);
         player.setTotalExperience(0);
         player.giveExp(exp - expCost);
      }
      player.sendTitle(messages.getString("title").replace("&", "§"), messages.getString("subtitle").replace("&", "§").replace("%cost%", String.valueOf(expCost)), 10, 30, 5);
      frozen.add(player);
      from.getWorld().spawnParticle(Particle.PORTAL, from.clone().add(0, .5, 0), 1000, .1, .5, .1, 1);
      to.getWorld().spawnParticle(Particle.PORTAL, to.clone().add(0, .5, 0), 1000, .1, .5, .1, 1);
      Bukkit.getScheduler().runTaskLater(this, () -> {
         Bukkit.getScheduler().runTaskLater(this, () -> frozen.remove(player), 5);
         from.getWorld().strikeLightningEffect(from);
         to.getWorld().strikeLightningEffect(to);
         player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
         to.getWorld().spawnParticle(Particle.LAVA, to.clone().add(0, .5, 0), 100);
      }, 40);
      
   }
   
   @EventHandler
   public void onPlayerMove(PlayerMoveEvent e) {
      if (frozen.contains(e.getPlayer()))
         if (!e.getTo().getBlock().equals(e.getFrom().getBlock())) e.setTo(e.getFrom().clone());
   }
   
   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      switch (command.getName().toLowerCase()) {
         case "fancytp":
            if (args.length < 1) return false;
            if (args[0].equals("config")) {
            
            }
            Player target = (Player)sender;
            if (args.length > 1) target = getServer().getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage("§cError! Could not find player `§o" + args[1] + "§c`.");
               return false;
            }
            String targetName = target.getName();
            switch (args[0].toLowerCase()) {
               case "toggle":
                  if (disabled.contains(targetName)) disabled.remove(targetName);
                  else disabled.add(targetName);
                  break;
               case "off":
                  if (!disabled.contains(targetName)) disabled.add(targetName);
                  break;
               case "on":
                  if (disabled.contains(targetName)) disabled.remove(targetName);
                  break;
               default:
                  return false;
            }
            config.set("exclude", disabled);
            saveConfig();
            return true;
         case "tp":
            StringBuilder com = new StringBuilder("minecraft:tp");
            for (String s : args)
               com.append(" ").append(s);
            getServer().dispatchCommand(sender, com.toString());
            return true;
      }
      return false;
   }
   
}

