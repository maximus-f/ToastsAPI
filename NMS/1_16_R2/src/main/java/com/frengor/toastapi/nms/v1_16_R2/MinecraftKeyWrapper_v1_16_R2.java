package com.frengor.toastapi.nms.v1_16_R2;

import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.ResourceKeyInvalidException;
import org.jetbrains.annotations.NotNull;

public class MinecraftKeyWrapper_v1_16_R2 extends MinecraftKeyWrapper {

    private final MinecraftKey key;

    public MinecraftKeyWrapper_v1_16_R2(@NotNull Object key) {
        this.key = (MinecraftKey) key;
    }

    public MinecraftKeyWrapper_v1_16_R2(@NotNull String namespace, @NotNull String key) {
        try {
            this.key = new MinecraftKey(namespace, key);
        } catch (ResourceKeyInvalidException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    @NotNull
    public String getNamespace() {
        return key.getNamespace();
    }

    @Override
    @NotNull
    public String getKey() {
        return key.getKey();
    }

    @Override
    public int compareTo(@NotNull MinecraftKeyWrapper obj) {
        return key.compareTo((MinecraftKey) obj.toNMS());
    }

    @Override
    @NotNull
    public MinecraftKey toNMS() {
        return key;
    }
}
