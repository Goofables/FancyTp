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
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public class FancyTp extends JavaPlugin implements Listener {
    //    private String cost;
    private static final int wait = 45;
    private final FileConfiguration config = getConfig();
    ArrayList<Player> frozen = new ArrayList<>(0);
    private List<String> disabledTp;
    private List<String> disabledDeath;
    private ConfigurationSection messages;
    
    @Override
    public void onEnable() {
        reloadCfg();
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    private void reloadCfg() {
        config.addDefault("excludeTp", new String[] {"MyPlayerName123"});
        config.addDefault("excludeDeath", new String[] {"MyPlayerName123"});
        disabledTp = config.getStringList("excludeTp");
        disabledDeath = config.getStringList("excludeDeath");
        if (!config.isConfigurationSection("messages")) config.createSection("messages");
        config.addDefault("messages.title", "&aWarping");
        config.addDefault("messages.subtitle", "&cDistance: &6%dist% blocks");
        /*config.addDefault("xpCost", ".5%dist%");
        config.addDefault("xpCost.p", ".5%dist%");
        config.addDefault("xpCost.r", "1");*/
        config.addDefault("tpWait", "45");
        messages = config.getConfigurationSection("messages");
        //        cost = config.getString("xpCost").replaceAll(";", " ");
        //wait = config.getString("tpWait").replaceAll(";", " ");
        config.options().copyDefaults(true);
        saveConfig();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (disabledDeath.contains(player.getName()) || player.hasPermission("fancytp.nodeatheffect")) return;
        
        // Player is hidden in supper vanish
        if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") ||
                Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) if (VanishAPI.isInvisible(player)) return;
        
        // Player is hidden in essentials
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Essentials ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
            User u = ess.getUser(player);
            if (u != null && u.isHidden()) return;
        }
        
        player.getLocation().getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().clone().add(0, 1, 0),
                500, 0, .25, 0, 0.1F);
        player.getLocation().getWorld().strikeLightningEffect(player.getLocation());
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        //System.out.println("Player: " + e.getPlayer() + " From: " + e.getFrom() + " To: " + e.getTo() + " Cause: " + e.getCause());
        //for (RegisteredListener rl : e.getHandlers().getRegisteredListeners())
        //   System.out.println(" -> Pl:" + rl.getPlugin().getName() + " RL: " + rl.getListener());
        final Player player = e.getPlayer();
        final Location from = e.getFrom().clone();
        final Location to = e.getTo().clone();
        
        if (disabledTp.contains(player.getName()) || player.hasPermission("fancytp.notpeffect")) return;
        if (frozen.contains(player)) return;
        
        // Player is hidden in supper vanish
        if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") ||
                Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) if (VanishAPI.isInvisible(player)) return;
        
        // Player is hidden or afk in essentials
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Essentials ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
            User u = ess.getUser(player);
            if (u != null && (u.isHidden() || (e.getCause() == TeleportCause.PLUGIN && u.isAfk()))) return;
        }
        
        // If it isnt a command ignore it
        if (!(e.getCause() == TeleportCause.COMMAND || e.getCause() == TeleportCause.PLUGIN)) return;
        
        final double distance = (from.getWorld().getName().equals(to.getWorld().getName()))?from.distance(to):-1.0d;
        
        /*
        // Get the equation solver
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("javascript");
        */
        
        // Calculate cost and wait
        //int expCost = 0;
        int tpWait = wait;
        /*
        try {
            Object tmp1 = engine.eval("cost = (" + cost.replace("%dist%", "(" + String.valueOf(distance) + ")") + ");");
            Object tmp2 = engine.eval("wait = (" + wait.replace("%dist%", "(" + String.valueOf(distance) + ")") + ");");
            player.sendMessage("cost:" + tmp1 + " wait: " + wait + " dist: " + distance);
        } catch (ScriptException e1) {
            System.out.println("Error in tp: ");
            e1.printStackTrace();
            return;
        }*/
        
        if (tpWait < 0) tpWait = 0;
        //if (expCost < 0) expCost = 0;
        
        // Cancel the event
        // Event is canceled here because it is the last place before the teleport task.
        e.setCancelled(true);
        
        /*
        // Exp cost management
        int exp = player.getTotalExperience();
        if ((player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) &&
                !player.hasPermission("fancytp.nocost")) if (expCost > exp) {
            player.sendMessage("§cNot enough exp! Required: §6" + expCost + "§c Currently Have: §6" + exp + "§c!");
            return;
        } else Bukkit.getScheduler().runTask(this, new ExpTask(this, player, expCost, tpWait + 5 /*45 ticks per tp*));
        else expCost = -1;
        */
        String title = messages.getString("title").replaceAll("&", "§");
        String sub = messages.getString("subtitle").replaceAll("&", "§").replaceAll("%dist%",
                (distance < 0.0d)?"Infinite":String.valueOf((int)distance));
        //(expCost >= 0)?String.valueOf(expCost):"free");
        player.sendTitle(title, sub, 10, 30, 5);
        
        
        frozen.add(player);
        Bukkit.getScheduler().runTask(this, new EffectTask(this, from, to, tpWait));
        Bukkit.getScheduler().runTaskLater(this, new TpTask(this, player, from, to), (long)tpWait);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (frozen.contains(e.getPlayer()))
            if (!e.getTo().getBlock().equals(e.getFrom().getBlock())) e.setTo(e.getFrom().clone());
    }
    
    @EventHandler
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        if (frozen.contains((Player)e.getEntity())) e.setCancelled(true);
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        if (e.getDamager().getType() == EntityType.PLAYER) return;
        if (frozen.contains((Player)e.getEntity())) e.setCancelled(true);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandLower = command.getName().toLowerCase();
        switch (commandLower) {
            case "fancytp":
            case "fancydeath":
                if (args.length < 1) return false;
                if ("config".equals(args[0])) {sender.sendMessage("§cError! Command not setup yet.");}
                String targetName = (args.length < 2)?sender.getName():args[1].replaceAll("&", "§");
                if (!sender.hasPermission("facnytp.toggle.others")) targetName = sender.getName();
                switch (args[0].toLowerCase()) {
                    case "toggle":
                        if ("fancytp".equals(commandLower)) {
                            if (disabledTp.contains(targetName)) disabledTp.remove(targetName);
                            else disabledTp.add(targetName);
                        } else {
                            if (disabledDeath.contains(targetName)) disabledDeath.remove(targetName);
                            else disabledDeath.add(targetName);
                        }
                        break;
                    case "off":
                        if ("fancytp".equals(commandLower)) {
                            if (!disabledTp.contains(targetName)) disabledTp.add(targetName);
                        } else {
                            if (!disabledDeath.contains(targetName)) disabledDeath.add(targetName);
                        }
                        break;
                    case "on":
                        if ("fancytp".equals(commandLower)) {
                            if (disabledTp.contains(targetName)) disabledTp.remove(targetName);
                        } else {
                            if (disabledDeath.contains(targetName)) disabledDeath.remove(targetName);
                        }
                        break;
                    case "reload":
                        if (sender.hasPermission("fancytp.reload")) reloadCfg();
                        else sender.sendMessage("§cError! No permissions!");
                        return true;
                    default:
                        return false;
                }
                config.set("excludeTp", disabledTp);
                config.set("excludeDeath", disabledDeath);
                saveConfig();
                if ("fancytp".equals(commandLower)) sender.sendMessage(
                        "§6FancyTp " + (disabledTp.contains(targetName)?"§cdisabled":"§aenabled") + " §6for player " +
                                targetName + '.');
                else sender.sendMessage(
                        "§6FancyDeath " + (disabledDeath.contains(targetName)?"§cdisabled":"§aenabled") +
                                " §6for player " + targetName + '.');
                return true;
            case "normaltp":
                StringBuilder com = new StringBuilder("minecraft:tp");
                for (String s : args)
                    com.append(' ').append(s);
                getServer().dispatchCommand(sender, com.toString());
                return true;
        }
        return false;
    }
    
}

