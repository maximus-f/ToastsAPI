package com.frengor.toastapi.nms.v1_21_R2.packets;

import com.frengor.toastapi.nms.util.ListSet;
import com.frengor.toastapi.nms.v1_21_R2.Util;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.frengor.toastapi.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_v1_21_R2 extends PacketPlayOutAdvancementsWrapper {

    private final ClientboundUpdateAdvancementsPacket packet;

    public PacketPlayOutAdvancementsWrapper_v1_21_R2() {
        this.packet = new ClientboundUpdateAdvancementsPacket(true, Collections.emptyList(), Collections.emptySet(), Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_21_R2(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        Map<ResourceLocation, AdvancementProgress> progressMap = Maps.newHashMapWithExpectedSize(toSend.size());
        for (Entry<AdvancementWrapper, Integer> e : toSend.entrySet()) {
            AdvancementWrapper adv = e.getKey();
            AdvancementHolder holder = (AdvancementHolder) adv.toNMS();
            AdvancementProgress progress = createAdvancementProgress(holder, e.getValue());
            progressMap.put((ResourceLocation) adv.getKey().toNMS(), progress);
        }
        this.packet = new ClientboundUpdateAdvancementsPacket(false, (Collection<AdvancementHolder>) ListSet.fromWrapperSet(toSend.keySet()), Collections.emptySet(), progressMap);
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_21_R2(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        this.packet = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), (Set<ResourceLocation>) ListSet.fromWrapperSet(toRemove), Collections.emptyMap());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }

    @NotNull
    private static AdvancementProgress createAdvancementProgress(@NotNull AdvancementHolder holder, int progression) {
        AdvancementProgress progress = new AdvancementProgress();
        progress.update(holder.value().requirements());

        for (int i = 0; i < progression; i++) {
            CriterionProgress criterionProgress = progress.getCriterion(String.valueOf(i));
            if (criterionProgress != null) {
                criterionProgress.grant();
            }
        }

        return progress;
    }
}
