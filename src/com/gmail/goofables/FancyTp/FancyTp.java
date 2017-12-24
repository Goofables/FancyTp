package com.gmail.goofables.FancyTp;

/**
 * Spigot Template created by ***REMOVED*** on {DATE}.
 * <p>
 * //TODO:
 */

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class FancyTp extends JavaPlugin implements Listener {
   ArrayList<Player> frozen = new ArrayList<>();
   private FileConfiguration config = getConfig();
   private List<String> disabled;
   private ConfigurationSection messages;
   private String cost;
   private String wait;
   
   @Override
   public void onEnable() {
      config.addDefault("exclude", new String[] {"MyPlayerName123"});
      disabled = config.getStringList("exclude");
      if (!config.isConfigurationSection("messages")) config.createSection("messages");
      config.addDefault("messages.title", "&aWarping");
      config.addDefault("messages.subtitle", "&cCost: &6%cost%");
      config.addDefault("xpCost", ".5%dist%");
      config.addDefault("xpCost.p", ".5%dist%");
      config.addDefault("xpCost.r", "1");
      config.addDefault("tpWait", "45");
      messages = config.getConfigurationSection("messages");
      cost = config.getString("xpCost").replaceAll(";", " ");
      wait = config.getString("tpWait").replaceAll(";", " ");
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
      if (disabled.contains(player.getName()) || player.hasPermission("fancytp.noeffect")) return;
      if (frozen.contains(player)) return;
      // Player is hidden in supper vanish
      if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish"))
         if (VanishAPI.isInvisible(player)) return;
      
      // Player is hidden
      if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
         Essentials ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
         User u = ess.getUser(player);
         if (u != null && u.isHidden()) return;
      }
      
      // If it isnt a command ignore it
      if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {return;}
      
      
      double distance = from.distance(to);
      
      // Get the equation solver
      ScriptEngineManager mgr = new ScriptEngineManager();
      ScriptEngine engine = mgr.getEngineByName("javascript");
      
      // Calculate cost and wait
      int expCost = 0;
      int tpWait = 0;
      try {
         Object tmp1 = engine.eval("cost = (" + cost.replace("%dist%", "(" + String.valueOf(distance) + ")") + ");");
         Object tmp2 = engine.eval("wait = (" + wait.replace("%dist%", "(" + String.valueOf(distance) + ")") + ");");
         player.sendMessage("cost:" + tmp1 + " wait: " + wait + " dist: " + distance);
      } catch (ScriptException e1) {
         System.out.println("Error in tp: ");
         e1.printStackTrace();
         return;
      }
      
      if (tpWait < 0) tpWait = 0;
      if (expCost < 0) expCost = 0;
      
      // Cancel the event
      e.setCancelled(true);
      
      // Exp cost management
      int exp = player.getTotalExperience();
      if ((player.getGameMode().equals(GameMode.SURVIVAL) || player.getGameMode().equals(GameMode.ADVENTURE)) && !player.hasPermission("fancytp.nocost"))
         if (expCost > exp) {
            player.sendMessage("§cNot enough exp! Required: §6" + expCost + "§c Currently Have: §6" + exp + "§c!");
            return;
         } else Bukkit.getScheduler().runTask(this, new ExpTask(this, player, expCost, tpWait + 5 /*45 ticks per tp*/));
      else expCost = -1;
      
      player.sendTitle(messages.getString("title").replace("&", "§"), messages.getString("subtitle").replace("&", "§").replace("%cost%", (expCost >= 0)?String.valueOf(expCost):"free"), 10, 30, 5);
      frozen.add(player);
      Bukkit.getScheduler().runTask(this, new EffectTask(this, from, to, tpWait));
      Bukkit.getScheduler().runTaskLater(this, new TpTask(this, player, from, to), tpWait);
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
            if (args[0].equals("config")) {}
            Player target = (Player)sender;
            if (args.length > 1) target = getServer().getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage("§cError! Could not find player `§o" + args[1] + "§c`.");
               return true;
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
            sender.sendMessage("§6FancyTp " + (disabled.contains(targetName)?"§cdisabled":"§aenabled") + " §6for player" + target.getDisplayName() + ".");
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

