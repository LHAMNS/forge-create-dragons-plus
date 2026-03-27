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
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Uses string-based serializer type instead of Factory pattern.
 * No CookingBookCategory in the builder (1.20.1 handles it internally).
 */
public class CookingRecipeBuilder extends BaseSingleItemRecipeBuilder<AbstractCookingRecipe, CookingRecipeBuilder> {
    private final String serializerType;
    private float experience;
    private int cookingTime;
    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();
    private String group = "";

    public CookingRecipeBuilder(@Nullable String directory, String serializerType, int cookingTime) {
        super(directory);
        this.serializerType = serializerType;
        this.cookingTime = cookingTime;
    }

    public CookingRecipeBuilder experience(float experience) {
        this.experience = experience;
        return this;
    }

    public CookingRecipeBuilder cookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
        return this;
    }

    public CookingRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        criteria.put(name, criterion);
        return this;
    }

    public CookingRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    protected CookingRecipeBuilder builder() {
        return this;
    }

    @Override
    public FinishedRecipe build() {
        if (id == null) {
            id = BuiltInRegistries.ITEM.getKey(result.getItem());
        }
        return new Result(id, group, serializerType, ingredient, result, experience, cookingTime);
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
        private final String serializerType;
        private final net.minecraft.world.item.crafting.Ingredient ingredient;
        private final net.minecraft.world.item.ItemStack result;
        private final float experience;
        private final int cookingTime;

        Result(ResourceLocation id, String group, String serializerType, net.minecraft.world.item.crafting.Ingredient ingredient, net.minecraft.world.item.ItemStack result, float experience, int cookingTime) {
            this.id = id;
            this.group = group;
            this.serializerType = serializerType;
            this.ingredient = ingredient;
            this.result = result;
            this.experience = experience;
            this.cookingTime = cookingTime;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }
            json.add("ingredient", ingredient.toJson());
            json.addProperty("result", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            json.addProperty("experience", experience);
            json.addProperty("cookingtime", cookingTime);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return switch (serializerType) {
                case "blasting" -> RecipeSerializer.BLASTING_RECIPE;
                case "smoking" -> RecipeSerializer.SMOKING_RECIPE;
                case "campfire_cooking" -> RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
                default -> RecipeSerializer.SMELTING_RECIPE;
            };
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
