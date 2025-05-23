package com.frengor.toastapi.nms.v1_21_R4.advancement;

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

public class AdvancementWrapper_v1_21_R4 extends AdvancementWrapper {

    private final AdvancementHolder advancementHolder;
    private final MinecraftKeyWrapper key;
    private final AdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_v1_21_R4(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = createAdvancementCriteria(maxProgression);
        AdvancementRequirements requirements = createAdvancementRequirements(advCriteria);
        Advancement advancement = new Advancement(
            Optional.empty(), 
            Optional.of((DisplayInfo) display.toNMS()), 
            AdvancementRewards.EMPTY, 
            advCriteria, 
            requirements, 
            false, 
            Optional.empty()
        );
        this.advancementHolder = new AdvancementHolder((ResourceLocation) key.toNMS(), advancement);
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_v1_21_R4(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = createAdvancementCriteria(maxProgression);
        AdvancementRequirements requirements = createAdvancementRequirements(advCriteria);
        Advancement advancement = new Advancement(
            Optional.of((ResourceLocation) parent.getKey().toNMS()), 
            Optional.of((DisplayInfo) display.toNMS()), 
            AdvancementRewards.EMPTY, 
            advCriteria, 
            requirements, 
            false, 
            Optional.empty()
        );
        this.advancementHolder = new AdvancementHolder((ResourceLocation) key.toNMS(), advancement);
        this.key = key;
        this.parent = parent;
        this.display = display;
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
        return this.advancementHolder.value().requirements().size();
    }

    @Override
    @NotNull
    public AdvancementHolder toNMS() {
        return advancementHolder;
    }

    @NotNull
    private static Map<String, Criterion<?>> createAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Maps.newHashMapWithExpectedSize(maxProgression);
        for (int i = 0; i < maxProgression; i++) {
            advCriteria.put(String.valueOf(i), new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
        }
        return advCriteria;
    }

    @NotNull
    private static AdvancementRequirements createAdvancementRequirements(@NotNull Map<String, Criterion<?>> advCriteria) {
        List<List<String>> list = new ArrayList<>(advCriteria.size());
        for (String name : advCriteria.keySet()) {
            list.add(List.of(name));
        }
        return new AdvancementRequirements(list);
    }
}
