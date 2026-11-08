package com.frengor.toastapi;

import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the frame type of a toast notification.
 */
public enum AdvancementFrameType {
    /**
     * Task frame type (default square frame).
     */
    TASK,

    /**
     * Goal frame type (fancy frame with a star).
     */
    GOAL,

    /**
     * Challenge frame type (fancy frame with a dragon).
     */
    CHALLENGE;

    /**
     * Gets the NMS wrapper for this frame type.
     *
     * @return The NMS wrapper
     */
    @NotNull
    public AdvancementFrameTypeWrapper getNMSWrapper() {
        return switch (this) {
            case TASK -> AdvancementFrameTypeWrapper.TASK;
            case GOAL -> AdvancementFrameTypeWrapper.GOAL;
            case CHALLENGE -> AdvancementFrameTypeWrapper.CHALLENGE;
        };
    }
}
