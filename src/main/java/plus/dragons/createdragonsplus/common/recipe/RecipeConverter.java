/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.common.recipe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.util.RecipeHolder;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A function that converts one recipe type to another, with optional caching support.
 * Cache is invalidated on resource reload.
 * <p>
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- uses our own RecipeHolder equivalent
 * to match upstream's {@code Function<RecipeHolder<K>, RecipeHolder<V>>} signature.
 *
 * @param <K> the source recipe type
 * @param <V> the target recipe type
 */
public interface RecipeConverter<K extends Recipe<?>, V extends Recipe<?>> extends Function<RecipeHolder<K>, RecipeHolder<V>> {
    Map<RecipeConverter<?, ?>, Runnable> CACHE_INVALIDATORS = new IdentityHashMap<>();

    @Mod.EventBusSubscriber(modid = CDPCommon.ID)
    class Events {
        @SubscribeEvent
        static void onAddReloadListener(final AddReloadListenerEvent event) {
            event.addListener((ResourceManagerReloadListener) resourceManager ->
                    CACHE_INVALIDATORS.values().forEach(Runnable::run));
        }
    }

    static <K extends Recipe<?>, V extends Recipe<?>> RecipeConverter<K, V> cached(
            CacheBuilder<Object, Object> cacheBuilder, RecipeConverter<K, V> converter) {
        var cache = cacheBuilder.build(new CacheLoader<RecipeHolder<K>, RecipeHolder<V>>() {
            @Override
            public RecipeHolder<V> load(RecipeHolder<K> key) {
                return converter.apply(key);
            }
        });
        RecipeConverter<K, V> result = cache::getUnchecked;
        CACHE_INVALIDATORS.put(result, cache::invalidateAll);
        return result;
    }
}
