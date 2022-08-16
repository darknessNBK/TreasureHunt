package com.lighty.TreasureHunt;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureHandler {

    @Getter private ArrayList<NPCHandler> treasures = new ArrayList<>();

    private boolean isBusy(Integer id){
        Boolean busy = Main.getConf().getBoolean("treasureLocations." + id + ".busy");
        if(busy == null) return true;
        else return busy;
    }

    private Location getTreasureLoc(Integer id){
        Location location = Main.getConf().getLocation("treasureLocations." + id + ".location");
        return location;
    }

    public String getRandomKey() {
        if(Main.getGenesisMode()) return "Genesis";

        int diasupply = Main.getConf().getInt("supplies.diamond");
        int golsupply = Main.getConf().getInt("supplies.gold");
        int silsupply = Main.getConf().getInt("supplies.silver");

        String key = Main.getKeys().next();

        if(key == "Diamond") {
            if(diasupply < 1) {
                key = "Gold";
            } else diasupply--;
        }
        if(key == "Gold") {
            if(golsupply < 1) {
                key = "Silver";
            } else golsupply--;
        }
        if(key == "Silver") {
            if(silsupply < 1) {
                key = "Generic";
            } else silsupply--;
        }

        Main.getConf().set("supplies.diamond", diasupply);
        Main.getConf().set("supplies.gold", golsupply);
        Main.getConf().set("supplies.silver", silsupply);
        Main.getInstance().saveConfig();

        return key;
    }

    public ItemStack getItemByKey(String key) {
        String itemName = Main.getConf().getString("keyItems." + key + ".name");
        String itemMaterial = Main.getConf().getString("keyItems." + key + ".material");
        Integer itemCustomModelData = Main.getConf().getInt("keyItems." + key + ".custommodeldata");

        ItemStack item = new ItemStack(Material.getMaterial(itemMaterial));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
        meta.setCustomModelData(itemCustomModelData);

        item.setItemMeta(meta);

        return item;
    }

    public void findTreasure(Player p, Integer entityID){
        if(!Main.getWallet().getNFTPlayer(p).isLinked()){
            p.sendMessage(ChatColor.RED + "You don't have a connected wallet to your account!");
            return;
        }
        if(p.getEnderChest().firstEmpty() == -1) {
            p.sendMessage(ChatColor.RED + "You don't have enough space in your vault for a key! Use /vault!");
            return;
        }
        NPCHandler npcHandler = null;
        for(NPCHandler npcHandler1 : treasures){
            if(npcHandler1.getNpc().getEntityID() == entityID){
                String key = getRandomKey();

                npcHandler = npcHandler1;

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Message");
                out.writeUTF("ALL");
                out.writeUTF(ChatColor.GREEN + "Player " + ChatColor.BOLD + p.getName() + ChatColor.GREEN + " found 1x " + key.toLowerCase() + " key!");

                p.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());

                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

                Methods.writeLog(p, Main.getWallet().getPrimaryWallet(p).getAddress(), npcHandler.getId(), key);

                p.getEnderChest().addItem(getItemByKey(key.toLowerCase()));
                p.sendActionBar(ChatColor.GREEN + "" + ChatColor.BOLD + "Use /vault to see your key!");

                break;
            }
        }
        if(npcHandler == null) return;
        if(!isBusy(npcHandler.getId())) return;
        npcHandler.hideEntityFromALL();

        Main.getConf().set("treasureLocations." + npcHandler.getId() + ".busy", false);
        Main.getInstance().saveConfig();
        treasures.remove(npcHandler);
    }

    public Integer findPerfectID(){
        ConfigurationSection sec = Main.getConf().getConfigurationSection("treasureLocations");
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> busyList = new ArrayList<>();
        for (String key : sec.getKeys(false)) {
            int keyInt = Integer.parseInt(key);
            if(isBusy(keyInt)) busyList.add(key);
            else list.add(key);
        }
        int i = -1;
        int trys = 0;
        if(list.size() == 0) return -1;
        while(true) {
            if(trys == list.size() + 10) break;
            int[] range = getRange(list);
            int randomNum = ThreadLocalRandom.current().nextInt(0,range[0]+1);
            if(!isBusy(randomNum)) {
                i = randomNum;
                break;
            }
            trys++;
        }
        return i;
    }

    public void spawnTreasure(){
        int maxlimit = Main.getConf().getInt("config.max-treasure-at-time");
        if(treasures.size()+1 > maxlimit) return;

        int treasureID = findPerfectID();
        Main.getInstance().getLogger().info("[NFT Treasure] Treasure spawned! ID: " + treasureID);
        if(treasureID == -1) return;

        NPCHandler npc = new NPCHandler();
        npc.spawnNPC(getTreasureLoc(treasureID), ChatColor.BOLD + "KEY MAKER");
        npc.setId(treasureID);
        treasures.add(npc);

        Main.getConf().set("treasureLocations." + npc.getId() + ".busy", true);
        Main.getInstance().saveConfig();
    }

    public void startFinding(){
        new BukkitRunnable(){
            int i = 0;
            int time = Main.getConf().getInt("config.spawn-frequency-in-minutes");
            public void run() {
                i++;
                if(i == (20*60)*time){
                    if(Main.configChance())
                         spawnTreasure();
                    i = 0;
                }
                for(NPCHandler npcHandler : treasures){
                    npcHandler.showToPlayer();
                }
            }
        }.runTaskTimer(Main.getInstance(),1,1);
    }

    public int[] getRange(ArrayList<String> strings){
        int max = 0;
        int min = 99999;
        for(String st : strings){
            int i = Integer.parseInt(st);
            if(i > max) max = i;
            if(i < min) min = i;
        }

        int[] myNum = {max, min};
        return myNum;
    }

}
