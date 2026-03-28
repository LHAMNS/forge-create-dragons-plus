/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A simple equivalent of NeoForge's {@code DeferredHolder<R, T>} for Forge 1.20.1.
 * Wraps a lazy registry lookup with type-safe generics.
 * <p>
 * Implements {@link Supplier} for backward compatibility. Provides {@link #get()},
 * {@link #value()}, and {@link #isBound()} to match the NeoForge API surface.
 *
 * @param <R> the registry type (e.g. {@code FanProcessingType})
 * @param <T> the specific value type (e.g. {@code FreezingFanProcessingType})
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final ResourceKey<? extends Registry<R>> registryKey;
    private final ResourceLocation id;
    private final ResourceKey<R> key;
    private volatile T cached;
    private volatile boolean resolved;

    private DeferredHolder(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id) {
        this.registryKey = registryKey;
        this.id = id;
        this.key = ResourceKey.create(registryKey, id);
    }

    /**
     * Creates a new DeferredHolder that will lazily look up the value from the registry
     * identified by the given registry key.
     * <p>
     * Supports both vanilla registries (from {@link net.minecraft.core.registries.BuiltInRegistries})
     * and modded registries (such as Create's custom registries) as long as they are
     * registered in the root registry.
     *
     * @param registryKey the registry key (e.g. {@code CreateRegistries.FAN_PROCESSING_TYPE},
     *                    {@code Registries.RECIPE_TYPE})
     * @param id          the resource location of the entry
     * @param <R>         the registry type
     * @param <T>         the specific value type
     * @return a new DeferredHolder
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id) {
        return new DeferredHolder<>(registryKey, id);
    }

    /**
     * Returns the registry key this holder is bound to.
     */
    public ResourceKey<? extends Registry<R>> getRegistryKey() {
        return registryKey;
    }

    /**
     * Returns the resource location of this entry.
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Returns the resolved entry key (combination of registry key and entry id).
     */
    public ResourceKey<R> getKey() {
        return key;
    }

    /**
     * Returns true if the backing registry contains an entry for this holder's id.
     * This performs a registry lookup if not yet resolved.
     */
    public boolean isBound() {
        return resolve() != null;
    }

    /**
     * Alias for {@link #isBound()}.
     */
    public boolean isPresent() {
        return isBound();
    }

    /**
     * Returns the resolved value when available, or empty if not bound.
     */
    public Optional<T> asOptional() {
        return Optional.ofNullable(resolve());
    }

    /**
     * Returns the resolved value. Throws if not bound.
     */
    @Override
    public T get() {
        T value = resolve();
        if (value == null) {
            throw new IllegalStateException("DeferredHolder not bound: " + key.location());
        }
        return value;
    }

    /**
     * Alias for {@link #get()}, matching NeoForge's DeferredHolder API.
     */
    public T value() {
        return get();
    }

    /**
     * Clears the cached resolution state so the next access will resolve again.
     * Useful when registry contents may have changed.
     */
    public void clearCache() {
        synchronized (this) {
            cached = null;
            resolved = false;
        }
    }

    @SuppressWarnings("unchecked")
    private T resolve() {
        if (resolved) {
            return cached;
        }
        synchronized (this) {
            if (resolved) {
                return cached;
            }
            // Look up the registry from the root registry of registries.
            // This works for both vanilla registries and modded registries (including Create's)
            // because all registries are registered in BuiltInRegistries.REGISTRY.
            Registry<?> registry = net.minecraft.core.registries.BuiltInRegistries.REGISTRY
                    .get(registryKey.location());
            if (registry != null) {
                cached = (T) registry.get(id);
            }
            if (cached != null) {
                resolved = true;
            }
            return cached;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DeferredHolder<?, ?> other)) {
            return false;
        }
        return registryKey.equals(other.registryKey) && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryKey, id);
    }

    @Override
    public String toString() {
        return "DeferredHolder[" + registryKey.location() + " -> " + id + "]";
    }
}
