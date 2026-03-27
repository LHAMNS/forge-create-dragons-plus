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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Uses FinishedRecipe/JSON serialization pattern.
 */
public class ShapelessRecipeBuilder extends BaseShapelessRecipeBuilder<ShapelessRecipe, ShapelessRecipeBuilder> {
    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();
    private String group = "";

    public ShapelessRecipeBuilder(@Nullable String directory) {
        super(directory);
    }

    public ShapelessRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        criteria.put(name, criterion);
        return this;
    }

    public ShapelessRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    protected ShapelessRecipeBuilder builder() {
        return this;
    }

    @Override
    public FinishedRecipe build() {
        if (id == null) {
            id = BuiltInRegistries.ITEM.getKey(result.getItem());
        }
        return new Result(id, group, ingredients, result);
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
        private final java.util.List<Ingredient> ingredients;
        private final net.minecraft.world.item.ItemStack result;

        Result(ResourceLocation id, String group, java.util.List<Ingredient> ingredients, net.minecraft.world.item.ItemStack result) {
            this.id = id;
            this.group = group;
            this.ingredients = ingredients;
            this.result = result;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }
            JsonArray ingredientsArray = new JsonArray();
            for (Ingredient ingredient : ingredients) {
                ingredientsArray.add(ingredient.toJson());
            }
            json.add("ingredients", ingredientsArray);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            if (result.getCount() > 1) {
                resultObj.addProperty("count", result.getCount());
            }
            json.add("result", resultObj);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
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
