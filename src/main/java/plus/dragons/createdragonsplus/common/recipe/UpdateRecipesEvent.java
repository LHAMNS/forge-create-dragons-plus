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

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import plus.dragons.createdragonsplus.mixin.minecraft.RecipeManagerAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Fired when the {@link RecipeManager} has reloaded and is about to sync the recipes
 * from the server to the client.
 *
 * <p>This event is not cancellable, and does not have a result.</p>
 *
 * <p>This event is fired on the {@linkplain MinecraftForge#EVENT_BUS game event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}, right after the
 * {@link TagsUpdatedEvent}. Therefore, updated tags and data maps can be retrieved in this event.</p>
 *
 * <p>Ported from NeoForge 1.21.1 to Forge 1.20.1 -- uses Recipe directly instead of RecipeHolder,
 * and adapts to the 1.20.1 RecipeManager internal structure which uses
 * {@code Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>} instead of Multimap.</p>
 */
public class UpdateRecipesEvent extends Event {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RecipeManager recipeManager;
    private final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType;
    private final Map<ResourceLocation, Recipe<?>> byName;
    private int added;
    private int removed;

    @Internal
    public UpdateRecipesEvent(RecipeManager recipeManager,
                              Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
                              Map<ResourceLocation, Recipe<?>> byName) {
        this.recipeManager = recipeManager;
        this.byType = byType;
        this.byName = byName;
    }

    /**
     * @return the {@link RecipeManager recipe manager}.
     */
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * Adds a {@link Recipe} to the {@link RecipeManager recipe manager}.
     *
     * @param id     the recipe's resource location id
     * @param recipe the recipe to add
     */
    public void addRecipe(ResourceLocation id, Recipe<?> recipe) {
        byType.computeIfAbsent(recipe.getType(), k -> new HashMap<>()).put(id, recipe);
        byName.put(id, recipe);
        added++;
    }

    /**
     * Removes a {@link Recipe} from the {@link RecipeManager recipe manager}.
     *
     * @param id     the recipe's resource location id
     * @param recipe the recipe to remove
     */
    public void removeRecipe(ResourceLocation id, Recipe<?> recipe) {
        Map<ResourceLocation, Recipe<?>> typeMap = byType.get(recipe.getType());
        if (typeMap != null) {
            typeMap.remove(id);
        }
        byName.remove(id);
        removed++;
    }

    @Internal
    public void apply() {
        // Build immutable copies for each type map
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> immutableByType = new HashMap<>();
        for (var entry : byType.entrySet()) {
            immutableByType.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
        }
        ((RecipeManagerAccessor) recipeManager).setRecipes(ImmutableMap.copyOf(immutableByType));
        ((RecipeManagerAccessor) recipeManager).setByName(ImmutableMap.copyOf(byName));
        LOGGER.debug("Added {} recipes to RecipeManager", added);
        LOGGER.debug("Removed {} recipes from RecipeManager", removed);
    }
}
