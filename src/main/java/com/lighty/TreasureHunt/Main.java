package com.lighty.TreasureHunt;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.nftworlds.wallet.api.WalletAPI;
import de.jeff_media.jefflib.JeffLib;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.jitse.npclib.NPCLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public final class Main extends JavaPlugin implements PluginMessageListener {
    @Getter private static Main instance;
    @Getter private static ProtocolManager manager;
    @Getter private static WalletAPI wallet;
    @Getter private static FileConfiguration conf;
    @Getter private static NPCLib library;
    @Getter private static TreasureHandler treasureHandler;
    @Getter private static RandomCollection<String> keys = new RandomCollection<String>();

    @Getter private static ArrayList<Community> communities = new ArrayList<Community>();

    @Getter @Setter private static Boolean privateServer = false;
    @Getter private static ArrayList<Community> allowedCommunities = new ArrayList<>();

    @Getter @Setter private static Boolean genesisMode = false;

    private void registerCommands(String[] cmds, CommandExecutor cmdExecutor)
    {
        for (String cmd : cmds)
        {
            getCommand(cmd).setExecutor(cmdExecutor);
        }
    }

    public static Boolean configChance() {
        float configChance = conf.getInt("config.spawn-chance") / 100;
        return Math.random() <= configChance;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        getServer().getPluginManager().registerEvents(new DropListener(), this);

        manager = ProtocolLibrary.getProtocolManager();
        instance = this;
        library = new NPCLib(this);
        wallet = new WalletAPI();
        this.saveDefaultConfig();
        conf = this.getConfig();
        JeffLib.init(this);

        getLogger().info("NFT Treasure Hunt plugin started!");

        // Key chances
        Double diamond = conf.getDouble("chances.diamond") / 100;
        Double gold = conf.getDouble("chances.gold") / 100;
        Double silver = conf.getDouble("chances.silver") / 100;
        Double generic = conf.getDouble("chances.generic") / 100;

        keys.add(diamond, "Diamond");
        keys.add(gold, "Gold");
        keys.add(silver, "Silver");
        keys.add(generic, "Generic");

        // Loading communities!
        FileUtils.forceMkdir(new File(this.getDataFolder() + "/communities"));

        getLogger().info("Loading the communities...");
        Methods.loadCommunities();
        getLogger().info("Loaded " + communities.size() + " communities!");

        treasureHandler = new TreasureHandler();
        treasureHandler.startFinding();

        InteractPacket pListener = new InteractPacket();
        pListener.addPacketListener();

        registerCommands(new String[] { "treasure", "vault" }, (CommandExecutor) new Commands());

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    Location city = Main.getConf().getLocation("city");
                    Location portalPos1 = Main.getConf().getLocation("portal.pos1");
                    Location portalPos2 = Main.getConf().getLocation("portal.pos2");

                    if(portalPos1 == null && portalPos2 == null) return;

                    if(!Methods.inCuboid(player.getLocation(), portalPos1, portalPos2)) return;

                    if(city == null) return;

                    boolean canGoToCity = false;

                    for (ItemStack item : player.getInventory().getStorageContents()) {
                        if(item != null) {
                            if (item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("genesis").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("diamond").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("gold").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("silver").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("generic").getItemMeta().getDisplayName())) {
                                canGoToCity = true;
                            }
                        }
                    }

                    for (ItemStack item : player.getEnderChest().getStorageContents()) {
                        if(item != null) {
                            if (item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("genesis").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("diamond").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("gold").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("silver").getItemMeta().getDisplayName()) || item.getItemMeta().getDisplayName().equals(Main.getTreasureHandler().getItemByKey("generic").getItemMeta().getDisplayName())) {
                                canGoToCity = true;
                            }
                        }
                    }

                    if(canGoToCity) player.teleport(city);
                    else player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "&cYou don't have a key to use this portal!"));
                }
            }
        }.runTaskTimer(this, 2, 2);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("SomeSubChannel")) {
            // Use the code sample in the 'Response' sections below to read
            // the data.
        }
    }

    public static void reload() {
        keys = new RandomCollection<String>();
        // Key chances
        Double diamond = conf.getDouble("chances.diamond") / 100;
        Double gold = conf.getDouble("chances.gold") / 100;
        Double silver = conf.getDouble("chances.silver") / 100;
        Double generic = conf.getDouble("chances.generic") / 100;

        keys.add(diamond, "Diamond");
        keys.add(gold, "Gold");
        keys.add(silver, "Silver");
        keys.add(generic, "Generic");
    }
}
