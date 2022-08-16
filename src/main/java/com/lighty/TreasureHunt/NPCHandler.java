package com.lighty.TreasureHunt;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class NPCHandler {

    @Getter private ArrayList<Player> showed = new ArrayList<>();
    @Getter @Setter private Integer id;
    @Getter private NPC npc;
    private Location loc;

    public void showToPlayer(){
        ArrayList<Player> nearbys = new ArrayList<>();

        Location loc = this.loc.clone();

        for(Entity entity : loc.getWorld().getNearbyEntities(loc, 10,10,10)){
            if(entity instanceof Player) nearbys.add((Player) entity);
        }

        for(Player player : nearbys){
            if(!showed.contains(player)){
                showed.add(player);
                showEntity(player);
            }
        }
        for(int i = 0; i < showed.size(); i++){
            Player player = showed.get(i);
            if(!nearbys.contains(player) && player != null && player.isOnline()){
                showed.remove(player);
                hideEntity(player);
            }
        }
    }

    public void showEntity(Player player){
        npc.setASyncSkinByUsername(Main.getInstance(), player, Main.getConf().getString("config.mc-skin"), (test, test2) -> {
            npc.spawnNPC(player);
            npc.getMetadata().setFrozenTicks(99999);
        });
        npc.removeFromTabList(player);
    }

    public void hideEntity(Player player){
        npc.destroyNPC(player);
    }

    public void hideEntityFromALL(){
        for(int i = 0; i < showed.size(); i++){
            Player p = showed.get(i);
            npc.destroyNPC(p);
        }
        Main.getTreasureHandler().getTreasures().remove(this);
    }

    public void spawnNPC(Location loc, String name){
        this.loc = loc;
        npc = new NPC(UUID.randomUUID(), loc, name);
    }

}
