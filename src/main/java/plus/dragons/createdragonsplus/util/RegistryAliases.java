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

package plus.dragons.createdragonsplus.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry alias utility for Forge 1.20.1.
 * <p>
 * NeoForge provides {@code Registry.addAlias()} to remap old registry IDs to new ones
 * when loading world data. Forge 1.20.1 lacks this API, so we implement it via Mixin
 * on {@link net.minecraft.core.MappedRegistry}.
 * <p>
 * Aliases are resolved at most one level deep (no chaining) to prevent infinite recursion.
 * Thread-safe via {@link ConcurrentHashMap}.
 */
public final class RegistryAliases {
    private static final Map<ResourceLocation, ResourceLocation> ALIASES = new ConcurrentHashMap<>();

    private RegistryAliases() {}

    /**
     * Registers an alias so that lookups for {@code oldId} will resolve to {@code newId}.
     *
     * @param oldId the old/deprecated registry ID
     * @param newId the current registry ID to resolve to
     * @throws NullPointerException if either argument is null
     */
    public static void addAlias(ResourceLocation oldId, ResourceLocation newId) {
        if (oldId == null) throw new NullPointerException("oldId must not be null");
        if (newId == null) throw new NullPointerException("newId must not be null");
        ALIASES.put(oldId, newId);
    }

    /**
     * Resolves an alias for the given ID.
     *
     * @param id the registry ID to look up
     * @return the remapped ID if an alias exists, or {@code null} if no alias is registered
     */
    @Nullable
    public static ResourceLocation resolve(ResourceLocation id) {
        return ALIASES.get(id);
    }
}
