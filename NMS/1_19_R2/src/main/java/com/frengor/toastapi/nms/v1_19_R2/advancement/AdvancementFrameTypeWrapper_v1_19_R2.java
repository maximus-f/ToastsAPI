package com.frengor.toastapi.nms.v1_19_R2.advancement;

import com.frengor.toastapi.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_19_R2 extends AdvancementFrameTypeWrapper {

    private final net.minecraft.advancements.FrameType mcFrameType;
    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_v1_19_R2(@NotNull FrameType frameType) {
        this.frameType = frameType;
        this.mcFrameType = switch (frameType) {
            case TASK -> net.minecraft.advancements.FrameType.TASK;
            case GOAL -> net.minecraft.advancements.FrameType.GOAL;
            case CHALLENGE -> net.minecraft.advancements.FrameType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public net.minecraft.advancements.FrameType toNMS() {
        return mcFrameType;
    }
}
