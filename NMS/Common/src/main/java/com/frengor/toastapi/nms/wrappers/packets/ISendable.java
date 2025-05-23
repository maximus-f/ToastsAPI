package com.frengor.toastapi.nms.wrappers.packets;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Interface for sending packets to players.
 */
public interface ISendable {

    /**
     * Send the packet to the specified player.
     *
     * @param player The player who will receive the packet.
     * @throws IllegalArgumentException If the specified player is {@code null}.
     */
    void sendTo(@NotNull Player player);

    /**
     * Send the packet to the specified players.
     *
     * @param players The players who will receive the packet. Eventually {@code null} values are ignored.
     * @throws IllegalArgumentException If the players array is {@code null}.
     */
    default void sendTo(@NotNull Player... players) {
        Preconditions.checkNotNull(players, "Players is null.");
        for (Player p : players) {
            if (p != null)
                sendTo(p);
        }
    }

    /**
     * Send the packet to the specified players.
     *
     * @param players The players who will receive the packet. Eventually {@code null} values are ignored.
     * @throws IllegalArgumentException If the players collection is {@code null}.
     */
    default void sendTo(@NotNull Collection<Player> players) {
        Preconditions.checkNotNull(players, "Collection<Player> is null.");
        for (Player p : players) {
            if (p != null)
                sendTo(p);
        }
    }

}
