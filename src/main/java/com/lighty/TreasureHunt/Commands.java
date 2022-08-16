package com.lighty.TreasureHunt;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (label.equalsIgnoreCase("vault")) {
                p.openInventory(p.getEnderChest());
                return true;
            }
            if (label.equalsIgnoreCase("treasure")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&m &e&m &e&m&r &6&lNFT TREASURE COMMANDS &e&m &e&m &e&m&r"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure create: &7&oCreates a treasure location at your location."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure remove [id]: &7&oRemoves the treasure location with given ID."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure list: &7&oGets you the list of treasure locations."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure tp [id]: &7&oTeleports you to the location of given treasure ID."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure spawn: &7&oSpawns a random treasure."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure setcity: &7&oSets the location of the city."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure setportal: &7&oSets the location of portal."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure communitymode: &7&oOpens/closes the community mode."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure addcommunity [contract] [name]: &7&oCreates community with given collection contract address."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure removecommunity [name]: &7&oRemoves the community."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure allowed [add/remove/list] <name>: &7&oModifies the allowed communities."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/treasure genesismode: &7&oSwitches to Genesis Treasure Hunt mode."));
                    return true;
                }
                if (args[0].equalsIgnoreCase("create")) {
                    ConfigurationSection sec = Main.getConf().getConfigurationSection("treasureLocations");
                    int id = Methods.findPerfectID(sec.getKeys(false).stream().toList());
                    Location loc = p.getLocation();
                    Main.getConf().set("treasureLocations." + id + ".location", loc);
                    Main.getConf().set("treasureLocations." + id + ".busy", false);
                    Main.getInstance().saveConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou've created a treasure location with ID " + id));
                }
                if (args[0].equalsIgnoreCase("remove")) {
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cYou must give an ID!"));
                        return true;
                    }
                    ConfigurationSection sec = Main.getConf().getConfigurationSection("treasureLocations");
                    List<String> list = sec.getKeys(false).stream().toList();
                    if (!list.contains(args[1])) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no treasure location with given ID!"));
                    } else {
                        Main.getConf().set("treasureLocations." + args[1], null);
                        Main.getInstance().saveConfig();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou've deleted the treasure location with ID " + args[1]));
                    }
                }
                if (args[0].equalsIgnoreCase("addcommunity")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cYou must give a contract address and a name: /treasure addcommunity [contract] [name]"));
                        return true;
                    }

                    ArrayList<Community> communities = Main.getCommunities();
                    Community community = new Community(args[2], args[1]);
                    communities.add(community);

                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCommunity added successfuly!"));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eName: &7" + args[2]));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eContract Address: &7" + args[1]));
                    return true;
                }
                if (args[0].equalsIgnoreCase("removecommunity")) {
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must give the name of the community!"));
                        return true;
                    }

                    ArrayList<Community> communities = Main.getCommunities();
                    Community givenCommunity = Methods.getCommunity(args[1]);

                    if(givenCommunity == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis community doesn't exist!"));
                        return true;
                    }

                    givenCommunity.remove();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully deleted the community!"));

                    return true;
                }
                if (args[0].equalsIgnoreCase("communitymode")) {
                    Main.setPrivateServer(!Main.getPrivateServer());
                    if(!Main.getPrivateServer()) { Main.getAllowedCommunities().clear(); }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou changed community mode to " + Main.getPrivateServer().toString() + "!"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&oYou can modify the allowed communites with: /treasure allowed [add/remove/list] [community name]"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("genesismode")) {
                    Main.setGenesisMode(!Main.getGenesisMode());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou changed Genesis Treasure Hunt mode to " + Main.getGenesisMode().toString() + "!"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("allowed")) {
                    if(!Main.getPrivateServer()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis server is not on community mode!"));
                        return true;
                    }

                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cWrong use! Example: /treasure allowed [add/remove/list] <community name>"));
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                        if(args.length < 3) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cWrong use! Example: /treasure allowed [add/remove/list] <community name>"));
                            return true;
                        }
                    }

                    if(args[1].equalsIgnoreCase("add")) {
                        Community com = Methods.getCommunity(args[2]);
                        if(com == null) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis community doesn't exist!")); return true; }
                        if(Main.getAllowedCommunities().contains(com)) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis community already is on the allowed list!")); return true; }

                        Main.getAllowedCommunities().add(com);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou added " + com.getName() + " to allowed communities!"));
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("remove")) {
                        Community com = Methods.getCommunity(args[2]);
                        if(com == null) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis community doesn't exist!")); return true; }

                        if(!Main.getAllowedCommunities().contains(com)) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cGiven community is not on the allowed list!")); return true; }

                        Main.getAllowedCommunities().remove(com);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully removed " + com.getName() + " from the allowed list!"));
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("list")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lALLOWED COMMUNITIES"));
                        for(Community com : Main.getAllowedCommunities().stream().toList()) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a" + com.getName()));
                        }
                        sender.sendMessage(" ");
                        return true;
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    ConfigurationSection sec = Main.getConf().getConfigurationSection("treasureLocations");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&m &e&m &e&m&r &6&lNFT TREASURE LIST &e&m &e&m &e&m&r"));
                    for (String st : sec.getKeys(false)) {
                        Methods.sendClickableCommand(p, ChatColor.translateAlternateColorCodes('&', "&8- &aTreasure ID " + st), "treasure tp " + st, ClickEvent.Action.SUGGEST_COMMAND);
                    }
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cYou must give an ID!"));
                        return true;
                    }
                    Methods.teleportPlayerToTreasure(p, args[1]);
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    Main.getInstance().reloadConfig();
                    Main.reload();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully reloaded the config!"));
                }
                if (args[0].equalsIgnoreCase("spawn")) {
                    if (Main.getTreasureHandler().findPerfectID() == -1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere are no locations to spawn!"));
                        return true;
                    }
                    Main.getTreasureHandler().spawnTreasure();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully spawned a treasure!"));
                }
                if (args[0].equalsIgnoreCase("setcity")) {
                    Main.getConf().set("city", p.getLocation());
                    Main.getInstance().saveConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully set the city location!"));
                }
                if (args[0].equalsIgnoreCase("setportal")) {

                    LocalSession WEPlayer = WorldEdit.getInstance().getSessionManager().findByName(p.getName());

                    try {
                        BlockVector3 max = WEPlayer.getSelection(WEPlayer.getSelectionWorld()).getMaximumPoint();
                        BlockVector3 min = WEPlayer.getSelection(WEPlayer.getSelectionWorld()).getMinimumPoint();

                        Location maxLoc = new Location(p.getWorld(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
                        Location minLoc = new Location(p.getWorld(), min.getBlockX(), min.getBlockY(), min.getBlockZ());

                        Main.getConf().set("portal.pos1", maxLoc);
                        Main.getConf().set("portal.pos2", minLoc);
                        Main.getInstance().saveConfig();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully set the portal location!"));
                        return true;

                    } catch (IncompleteRegionException e) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEncountered an error during the process! Please make sure you made the selection with WorldEdit!"));
                        e.printStackTrace();
                        return true;
                    }
                }
            }
        }
        return false;
    }}
