package com.lighty.TreasureHunt;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class DropListener implements Listener {
    @EventHandler
    public void onDropKey(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        if (item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("genesis").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("diamond").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("gold").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("silver").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("generic").getItemMeta().getDisplayName())) {
            e.getPlayer().sendActionBar(ChatColor.RED + "You can not drop your keys!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if(!Methods.canJoin(player)) {
            player.kickPlayer("This server currently on a community mode! You don't have any NFT from the allowed collections!");
        }
    }
}
