package com.frengor.toastapi.nms.v26_3_R1.packets;

import com.frengor.toastapi.nms.util.ListSet;
import com.frengor.toastapi.nms.v26_3_R1.Util;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket.PositionedAdvancement;
import net.minecraft.resources.Identifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_v26_3_R1 extends PacketPlayOutAdvancementsWrapper {

    private final ClientboundUpdateAdvancementsPacket packet;

    public PacketPlayOutAdvancementsWrapper_v26_3_R1() {
        this.packet = new ClientboundUpdateAdvancementsPacket(true, Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), true);
    }

    public PacketPlayOutAdvancementsWrapper_v26_3_R1(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        Map<Identifier, AdvancementProgress> map = Maps.newHashMapWithExpectedSize(toSend.size());
        for (Entry<AdvancementWrapper, Integer> e : toSend.entrySet()) {
            AdvancementWrapper adv = e.getKey();
            map.put((Identifier) adv.getKey().toNMS(), Util.getAdvancementProgress((PositionedAdvancement) adv.toNMS(), e.getValue()));
        }
        List<PositionedAdvancement> advancements = toSend.keySet().stream()
                .map(adv -> (PositionedAdvancement) adv.toNMS()).toList();
        this.packet = new ClientboundUpdateAdvancementsPacket(false, advancements, Collections.emptySet(), map, true);
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v26_3_R1(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        this.packet = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), (Set<Identifier>) ListSet.fromWrapperSet(toRemove), Collections.emptyMap(), true);
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
