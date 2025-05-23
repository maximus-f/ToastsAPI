package com.frengor.toastapi.nms.v1_21_R3.packets;

import com.frengor.toastapi.nms.util.ListSet;
import com.frengor.toastapi.nms.v1_21_R3.Util;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_v1_21_R3 extends PacketPlayOutAdvancementsWrapper {

    private final ClientboundUpdateAdvancementsPacket packet;

    public PacketPlayOutAdvancementsWrapper_v1_21_R3() {
        this.packet = new ClientboundUpdateAdvancementsPacket(true, Collections.emptyList(), Collections.emptySet(), Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_21_R3(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMapWithExpectedSize(toSend.size());
        for (Entry<AdvancementWrapper, Integer> e : toSend.entrySet()) {
            AdvancementWrapper adv = e.getKey();
            // For toast notifications, we just need a simple progress
            AdvancementProgress progress = new AdvancementProgress();
            progress.update(((Advancement) adv.toNMS()).getCriteria(), ((Advancement) adv.toNMS()).getRequirements());
            progress.getCriterion("0").grant();
            map.put((ResourceLocation) adv.getKey().toNMS(), progress);
        }
        this.packet = new ClientboundUpdateAdvancementsPacket(false, (Collection<Advancement>) ListSet.fromWrapperSet(toSend.keySet()), Collections.emptySet(), map);
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_21_R3(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        this.packet = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), (Set<ResourceLocation>) ListSet.fromWrapperSet(toRemove), Collections.emptyMap());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
