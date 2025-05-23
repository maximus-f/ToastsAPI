package com.frengor.toastapi.nms.wrappers.advancement;

import com.frengor.toastapi.nms.util.ReflectionUtil;
import com.frengor.toastapi.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

/**
 * {@code PreparedAdvancementWrapper} instances can be converted into an {@link AdvancementWrapper}
 * using {@link #toRootAdvancementWrapper()} or {@link #toBaseAdvancementWrapper(AdvancementWrapper)}.
 */
public abstract class PreparedAdvancementWrapper {

    private static Constructor<? extends PreparedAdvancementWrapper> constructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PreparedAdvancementWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            constructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class, AdvancementDisplayWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@code PreparedAdvancementWrapper}.
     *
     * @param key The namespaced key wrapper of the advancement.
     * @param display The display wrapper of the advancement.
     * @param maxProgression The maximum progression of the advancement.
     * @return A new {@code PreparedAdvancementWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PreparedAdvancementWrapper craft(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) throws ReflectiveOperationException {
        return constructor.newInstance(key, display, maxProgression);
    }

    /**
     * Gets the namespaced key wrapper of the advancement.
     *
     * @return The namespaced key wrapper of the advancement.
     */
    @NotNull
    public abstract MinecraftKeyWrapper getKey();

    /**
     * Gets the display wrapper of the advancement.
     *
     * @return The display wrapper of the advancement.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper getDisplay();

    /**
     * Gets the maximum progression of the advancement.
     *
     * @return The maximum progression of the advancement.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getMaxProgression();

    /**
     * Converts this {@code PreparedAdvancementWrapper} into an {@link AdvancementWrapper} without a parent advancement.
     *
     * @return A new {@link AdvancementWrapper} derived from this {@code PreparedAdvancementWrapper}.
     */
    @NotNull
    public abstract AdvancementWrapper toRootAdvancementWrapper();

    /**
     * Converts this {@code PreparedAdvancementWrapper} into an {@link AdvancementWrapper} which has a parent advancement.
     *
     * @param parent The parent of the returned advancement.
     * @return A new {@link AdvancementWrapper} derived from this {@code PreparedAdvancementWrapper}.
     */
    @NotNull
    public abstract AdvancementWrapper toBaseAdvancementWrapper(@NotNull AdvancementWrapper parent);

    @Override
    public String toString() {
        return "PreparedAdvancementWrapper{key=" + getKey() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreparedAdvancementWrapper that = (PreparedAdvancementWrapper) o;

        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
