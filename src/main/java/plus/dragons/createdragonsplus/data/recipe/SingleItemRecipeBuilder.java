/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data.recipe;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Uses FinishedRecipe/JSON serialization pattern.
 * In 1.20.1 stonecutting is the primary SingleItemRecipe type.
 */
public class SingleItemRecipeBuilder extends BaseSingleItemRecipeBuilder<SingleItemRecipe, SingleItemRecipeBuilder> {
    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();
    private String group = "";

    public SingleItemRecipeBuilder(@Nullable String directory) {
        super(directory);
    }

    public SingleItemRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        criteria.put(name, criterion);
        return this;
    }

    public SingleItemRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    protected SingleItemRecipeBuilder builder() {
        return this;
    }

    @Override
    public FinishedRecipe build() {
        if (id == null) {
            id = BuiltInRegistries.ITEM.getKey(result.getItem());
        }
        return new Result(id, group, ingredient, result);
    }

    @Override
    public @Nullable Advancement.Builder buildAdvancement() {
        if (id == null) {
            id = BuiltInRegistries.ITEM.getKey(result.getItem());
        }
        if (this.criteria.isEmpty()) {
            return null;
        }
        var builder = Advancement.Builder.advancement();
        this.criteria.forEach(builder::addCriterion);
        return builder;
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final net.minecraft.world.item.crafting.Ingredient ingredient;
        private final net.minecraft.world.item.ItemStack result;

        Result(ResourceLocation id, String group, net.minecraft.world.item.crafting.Ingredient ingredient, net.minecraft.world.item.ItemStack result) {
            this.id = id;
            this.group = group;
            this.ingredient = ingredient;
            this.result = result;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }
            json.add("ingredient", ingredient.toJson());
            json.addProperty("result", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            json.addProperty("count", result.getCount());
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.STONECUTTER;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
