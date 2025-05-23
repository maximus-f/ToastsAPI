package com.frengor.toastapi.nms.v1_16_R2.advancement;

import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.server.v1_16_R2.AdvancementFrameType;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_16_R2 extends AdvancementFrameTypeWrapper {

    private final AdvancementFrameType mcFrameType;
    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_v1_16_R2(@NotNull FrameType frameType) {
        this.frameType = frameType;
        this.mcFrameType = switch (frameType) {
            case TASK -> AdvancementFrameType.TASK;
            case GOAL -> AdvancementFrameType.GOAL;
            case CHALLENGE -> AdvancementFrameType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public AdvancementFrameType toNMS() {
        return mcFrameType;
    }
}
