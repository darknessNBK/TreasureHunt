package com.lighty.TreasureHunt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class InteractPacket {
    public void addPacketListener() {
        Main.getManager().addPacketListener(new PacketAdapter(Main.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                PacketContainer packet = e.getPacket();
                if(packet.getType() != PacketType.Play.Client.USE_ENTITY) return;
                int entityID = packet.getIntegers().read(0);
                Main.getTreasureHandler().findTreasure(e.getPlayer(), entityID);
            }
        });
    }
}