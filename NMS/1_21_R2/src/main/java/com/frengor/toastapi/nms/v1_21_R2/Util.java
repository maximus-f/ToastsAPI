package com.frengor.toastapi.nms.v1_21_R2;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Util {

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
