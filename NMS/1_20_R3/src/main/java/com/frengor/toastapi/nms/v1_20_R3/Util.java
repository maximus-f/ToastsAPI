package com.frengor.toastapi.nms.v1_20_R3;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.ImpossibleTrigger.TriggerInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class Util {

    @NotNull
    public static Map<String, Criterion<?>> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Preconditions.checkArgument(maxProgression >= 1, "Max progression must be >= 1.");

        Map<String, Criterion<?>> advCriteria = Maps.newHashMapWithExpectedSize(maxProgression);
        for (int i = 0; i < maxProgression; i++) {
            advCriteria.put(String.valueOf(i), new Criterion<>(new ImpossibleTrigger(), new TriggerInstance()));
        }

        return advCriteria;
    }

    @NotNull
    public static AdvancementRequirements getAdvancementRequirements(@NotNull Map<String, Criterion<?>> advCriteria) {
        Preconditions.checkNotNull(advCriteria, "Advancement criteria map is null.");

        // Convert to List<List<String>> for 1.20.4+ API
        java.util.List<java.util.List<String>> requirements = new java.util.ArrayList<>();
        for (String name : advCriteria.keySet()) {
            java.util.List<String> requirement = new java.util.ArrayList<>();
            requirement.add(name);
            requirements.add(requirement);
        }

        return new AdvancementRequirements(requirements);
    }

    @NotNull
    public static AdvancementProgress getAdvancementProgress(@NotNull AdvancementHolder mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        Preconditions.checkNotNull(mcAdv, "NMS Advancement is null.");
        Preconditions.checkArgument(progression >= 0, "Progression must be >= 0.");

        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.update(mcAdv.value().requirements());

        for (int i = 0; i < progression; i++) {
            CriterionProgress criteriaPrg = advPrg.getCriterion(String.valueOf(i));
            if (criteriaPrg != null) {
                criteriaPrg.grant();
            }
        }

        return advPrg;
    }

    @NotNull
    public static Component fromString(@NotNull String string) {
        if (string == null || string.isEmpty()) {
            return CommonComponents.EMPTY;
        }
        return CraftChatMessage.fromStringOrNull(string, true);
    }

    @NotNull
    public static Component fromComponent(@NotNull BaseComponent component) {
        if (component == null) {
            return CommonComponents.EMPTY;
        }
        Component base = CraftChatMessage.fromJSONOrNull(ComponentSerializer.toString(component));
        return base == null ? CommonComponents.EMPTY : base;
    }

    public static void sendTo(@NotNull Player player, @NotNull Packet<?> packet) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(packet, "Packet is null.");
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
