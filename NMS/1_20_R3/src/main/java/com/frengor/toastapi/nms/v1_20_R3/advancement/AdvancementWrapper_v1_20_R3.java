package com.frengor.toastapi.nms.v1_20_R3.advancement;

import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.frengor.toastapi.nms.wrappers.advancement.AdvancementWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdvancementWrapper_v1_20_R3 extends AdvancementWrapper {

    private final AdvancementHolder advancement;
    private final MinecraftKeyWrapper key;
    private final AdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_v1_20_R3(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = createSimpleCriteria();
        AdvancementRequirements requirements = createAdvancementRequirements(advCriteria);
        Advancement adv = new Advancement(Optional.empty(), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, requirements, false, Optional.empty());
        this.advancement = new AdvancementHolder((ResourceLocation) key.toNMS(), adv);
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_v1_20_R3(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = createSimpleCriteria();
        AdvancementRequirements requirements = createAdvancementRequirements(advCriteria);
        Advancement adv = new Advancement(Optional.of((ResourceLocation) parent.getKey().toNMS()), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, requirements, false, Optional.empty());
        this.advancement = new AdvancementHolder((ResourceLocation) key.toNMS(), adv);
        this.key = key;
        this.parent = parent;
        this.display = display;
    }

    private static Map<String, Criterion<?>> createSimpleCriteria() {
        Map<String, Criterion<?>> advCriteria = Maps.newHashMapWithExpectedSize(1);
        advCriteria.put("0", new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
        return advCriteria;
    }

    private static AdvancementRequirements createAdvancementRequirements(Map<String, Criterion<?>> advCriteria) {
        List<List<String>> list = new ArrayList<>(advCriteria.size());
        for (String name : advCriteria.keySet()) {
            list.add(List.of(name));
        }
        return new AdvancementRequirements(list);
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
    public AdvancementHolder toNMS() {
        return advancement;
    }
}
