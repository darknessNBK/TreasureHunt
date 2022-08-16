package com.lighty.TreasureHunt;

import com.nftworlds.wallet.objects.Network;
import de.leonhard.storage.Json;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Date;
import java.util.List;

public class Methods {

    public static int findPerfectID(List<?> list){
        int id = 0;
        for(int i = 0; i <= list.size(); i++){
            if(!list.contains(String.valueOf(i))){
                id = i;
                break;
            }
        }
        return id;
    }

    public static void sendClickableCommand(Player player, String message, String command, ClickEvent.Action action) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        component.setClickEvent(new ClickEvent(action, "/" + command));
        player.spigot().sendMessage(component);
    }

    public static void teleportPlayerToTreasure(Player p, String id){
        ConfigurationSection sec = Main.getConf().getConfigurationSection("treasureLocations");
        List<String> list = sec.getKeys(false).stream().toList();
        if(!list.contains(id)){
            p.sendMessage("This id doesn't exists.");
            return;
        }

        Location location = Main.getConf().getLocation("treasureLocations." + id + ".location");
        p.teleport(location);
    }

    public static void writeLog(Player player, String walletAdress, Integer treasureID, String key) {
        Date now = new Date();
        String log = "[" + now.toString() + "] " + player.getName() + " found 1x " + key.toLowerCase() + " key in Location ID " + treasureID + "! (Wallet: " + walletAdress + ")";

        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(Main.getInstance().getDataFolder() + "/treasurelog.txt", true));
            output.newLine();
            output.write(log);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean inCuboid(Location origin, Location l2, Location l1){
        return new IntRange(l1.getX(), l2.getX()).containsDouble(origin.getX())
                && new IntRange(l1.getY(), l2.getY()).containsDouble(origin.getY())
                &&  new IntRange(l1.getZ(), l2.getZ()).containsDouble(origin.getZ());
    }

    public static void loadCommunities() {
        for(File file : new File(Main.getInstance().getDataFolder() + "/communities").listFiles()){
            if(file.isFile() && file.getName().contains(".json")){
                Json config = new Json(file);
                Community community = new Community(config.getString("name"), config.getString("contract"));
                Main.getCommunities().add(community);
            }
        }
    }

    public static Community getCommunity(String name) {
        Community community = null;
        for(Community c : Main.getCommunities()) {
            if(c.getName().equalsIgnoreCase(name)) community = c;
        }
        return community;
    }

    @SneakyThrows
    public static boolean canJoin(Player player) {
        if(!Main.getPrivateServer()) return true;

        Boolean pass = false;

        if(Main.getAllowedCommunities().size() != 0) {
            for (Community community : Main.getAllowedCommunities()) {
                if (Main.getWallet().getPrimaryWallet(player).doesPlayerOwnNFTInCollection(Network.ETHEREUM, community.getContract())) {
                    pass = true;
                }
                ;
            }
        }

        if(player.isOp()) pass = true;
        return pass;
    }

}
