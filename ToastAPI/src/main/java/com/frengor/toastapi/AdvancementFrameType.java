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
    TASK(AdvancementFrameTypeWrapper.TASK),
    
    /**
     * Goal frame type (fancy frame with a star).
     */
    GOAL(AdvancementFrameTypeWrapper.GOAL),
    
    /**
     * Challenge frame type (fancy frame with a dragon).
     */
    CHALLENGE(AdvancementFrameTypeWrapper.CHALLENGE);

    private final AdvancementFrameTypeWrapper nmsWrapper;

    AdvancementFrameType(@NotNull AdvancementFrameTypeWrapper nmsWrapper) {
        this.nmsWrapper = nmsWrapper;
    }

    /**
     * Gets the NMS wrapper for this frame type.
     *
     * @return The NMS wrapper
     */
    @NotNull
    public AdvancementFrameTypeWrapper getNMSWrapper() {
        return nmsWrapper;
    }
} 