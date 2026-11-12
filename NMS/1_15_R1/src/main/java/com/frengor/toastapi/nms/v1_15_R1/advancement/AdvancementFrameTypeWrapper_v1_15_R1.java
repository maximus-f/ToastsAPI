package com.frengor.toastapi.nms.v1_15_R1.advancement;

import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.server.v1_15_R1.AdvancementFrameType;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_15_R1 extends AdvancementFrameTypeWrapper {

    private final AdvancementFrameType mcFrameType;
    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_v1_15_R1(@NotNull FrameType frameType) {
        this.frameType = frameType;
        switch (frameType) {
            case TASK: this.mcFrameType = AdvancementFrameType.TASK; break;
            case GOAL: this.mcFrameType = AdvancementFrameType.GOAL; break;
            case CHALLENGE: this.mcFrameType = AdvancementFrameType.CHALLENGE; break;
            default: throw new AssertionError(frameType);
        }
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
