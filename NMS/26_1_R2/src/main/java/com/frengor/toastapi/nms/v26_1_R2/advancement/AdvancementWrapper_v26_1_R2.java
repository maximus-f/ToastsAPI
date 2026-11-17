package com.frengor.toastapi.nms.v26_1_R2.advancement;

import com.frengor.toastapi.nms.v26_1_R2.Util;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Optional;

public class AdvancementWrapper_v26_1_R2 extends AdvancementWrapper {

    private final AdvancementHolder advancementHolder;
    private final MinecraftKeyWrapper key;

    public AdvancementWrapper_v26_1_R2(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Util.getAdvancementCriteria(maxProgression);
        Advancement advancement = new Advancement(Optional.empty(), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, Util.getAdvancementRequirements(advCriteria), false, Optional.empty());
        this.advancementHolder = new AdvancementHolder((Identifier) key.toNMS(), advancement);
        this.key = key;
    }

    public AdvancementWrapper_v26_1_R2(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Util.getAdvancementCriteria(maxProgression);
        Advancement advancement = new Advancement(Optional.of((Identifier) parent.getKey().toNMS()), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, Util.getAdvancementRequirements(advCriteria), false, Optional.empty());
        this.advancementHolder = new AdvancementHolder((Identifier) key.toNMS(), advancement);
        this.key = key;
    }

    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxProgression() {
        return this.advancementHolder.value().requirements().size();
    }

    @Override
    @NotNull
    public AdvancementHolder toNMS() {
        return advancementHolder;
    }
}
