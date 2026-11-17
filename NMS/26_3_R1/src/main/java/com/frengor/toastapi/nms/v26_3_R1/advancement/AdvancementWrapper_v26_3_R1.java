package com.frengor.toastapi.nms.v26_3_R1.advancement;

import com.frengor.toastapi.nms.v26_3_R1.Util;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket.PositionedAdvancement;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Optional;

public class AdvancementWrapper_v26_3_R1 extends AdvancementWrapper {

    private final PositionedAdvancement positionedAdvancement;
    private final MinecraftKeyWrapper key;

    public AdvancementWrapper_v26_3_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Util.getAdvancementCriteria(maxProgression);
        Advancement advancement = new Advancement(Optional.empty(), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, Util.getAdvancementRequirements(advCriteria), false, Optional.empty());
        this.positionedAdvancement = new PositionedAdvancement(new AdvancementHolder((Identifier) key.toNMS(), advancement), display.getX(), display.getY());
        this.key = key;
    }

    public AdvancementWrapper_v26_3_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Util.getAdvancementCriteria(maxProgression);
        Advancement advancement = new Advancement(Optional.of((Identifier) parent.getKey().toNMS()), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, Util.getAdvancementRequirements(advCriteria), false, Optional.empty());
        this.positionedAdvancement = new PositionedAdvancement(new AdvancementHolder((Identifier) key.toNMS(), advancement), display.getX(), display.getY());
        this.key = key;
    }

    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxProgression() {
        return this.positionedAdvancement.advancement().value().requirements().size();
    }

    @Override
    @NotNull
    public PositionedAdvancement toNMS() {
        return positionedAdvancement;
    }
}
