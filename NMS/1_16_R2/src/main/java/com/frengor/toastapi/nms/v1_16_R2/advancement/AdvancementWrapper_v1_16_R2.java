package com.frengor.toastapi.nms.v1_16_R2.advancement;

import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class AdvancementWrapper_v1_16_R2 extends AdvancementWrapper {

    private final Advancement advancement;
    private final MinecraftKeyWrapper key;
    private final AdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_v1_16_R2(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion> advCriteria = createSimpleCriteria();
        this.advancement = new Advancement((ResourceLocation) key.toNMS(), null, (DisplayInfo) display.toNMS(), AdvancementRewards.EMPTY, advCriteria, new String[][]{{"0"}}, false);
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_v1_16_R2(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion> advCriteria = createSimpleCriteria();
        this.advancement = new Advancement((ResourceLocation) key.toNMS(), (Advancement) parent.toNMS(), (DisplayInfo) display.toNMS(), AdvancementRewards.EMPTY, advCriteria, new String[][]{{"0"}}, false);
        this.key = key;
        this.parent = parent;
        this.display = display;
    }

    private static Map<String, Criterion> createSimpleCriteria() {
        Map<String, Criterion> advCriteria = Maps.newHashMapWithExpectedSize(1);
        advCriteria.put("0", new Criterion(new ImpossibleTrigger.TriggerInstance()));
        return advCriteria;
    }

    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    @Nullable
    public AdvancementWrapper getParent() {
        return parent;
    }

    @NotNull
    public AdvancementDisplayWrapper getDisplay() {
        return display;
    }

    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxProgression() {
        return 1;
    }

    @Override
    @NotNull
    public Advancement toNMS() {
        return advancement;
    }
}
