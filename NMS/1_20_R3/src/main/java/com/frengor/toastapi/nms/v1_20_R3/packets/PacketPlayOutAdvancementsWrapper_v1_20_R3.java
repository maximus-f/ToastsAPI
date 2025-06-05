package com.frengor.toastapi.nms.v1_20_R3.packets;

import com.frengor.toastapi.nms.util.ListSet;
import com.frengor.toastapi.nms.v1_20_R3.Util;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_v1_20_R3 extends PacketPlayOutAdvancementsWrapper {

    private final ClientboundUpdateAdvancementsPacket packet;

    public PacketPlayOutAdvancementsWrapper_v1_20_R3() {
        this.packet = new ClientboundUpdateAdvancementsPacket(true, Collections.emptyList(), Collections.emptySet(), Collections.emptyMap());
    }

    public PacketPlayOutAdvancementsWrapper_v1_20_R3(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMapWithExpectedSize(toSend.size());
        for (Map.Entry<AdvancementWrapper, Integer> e : toSend.entrySet()) {
            AdvancementWrapper adv = e.getKey();
            map.put((ResourceLocation) adv.getKey().toNMS(), Util.getAdvancementProgress((AdvancementHolder) adv.toNMS(), e.getValue().intValue()));
        }
        this.packet = new ClientboundUpdateAdvancementsPacket(false, (Collection) ListSet.fromWrapperSet(toSend.keySet()), Collections.emptySet(), map);
    }

    public PacketPlayOutAdvancementsWrapper_v1_20_R3(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        this.packet = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), (Set) ListSet.fromWrapperSet(toRemove), Collections.emptyMap());
    }

    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, (Packet) this.packet);
    }
}
