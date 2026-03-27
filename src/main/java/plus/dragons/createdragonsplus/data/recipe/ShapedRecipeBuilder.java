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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.recipe.BaseRecipeBuilder;
import plus.dragons.createdragonsplus.data.recipe.integration.IntegrationResultRecipe;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Uses FinishedRecipe/JSON serialization pattern instead of RecipeHolder/Codec.
 */
public class ShapedRecipeBuilder extends BaseRecipeBuilder<net.minecraft.world.item.crafting.ShapedRecipe, ShapedRecipeBuilder> {
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private int width = 0;
    private final List<String> pattern = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();
    private String group = "";
    private boolean showNotification = true;

    public ShapedRecipeBuilder(@Nullable String directory) {
        super(directory);
    }

    public ShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        return define(symbol, Ingredient.of(tag));
    }

    public ShapedRecipeBuilder define(Character symbol, ItemLike item) {
        return define(symbol, Ingredient.of(item));
    }

    public ShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if (key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            key.put(symbol, ingredient);
            return this;
        }
    }

    public ShapedRecipeBuilder pattern(String line) {
        Preconditions.checkArgument(!line.isEmpty(), "Pattern line must not be empty");
        if (width == 0) {
            width = line.length();
        } else if (width != line.length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        pattern.add(line);
        return this;
    }

    public ShapedRecipeBuilder output(ItemLike item) {
        this.result = new ItemStack(item);
        return this;
    }

    public ShapedRecipeBuilder output(ItemLike item, int count) {
        this.result = new ItemStack(item, count);
        return this;
    }

    public ShapedRecipeBuilder output(ItemStack stack) {
        this.result = stack;
        return this;
    }

    public IntegrationResultRecipe.Builder output(ResourceLocation result) {
        return new IntegrationResultRecipe.Builder(this, this.result, result);
    }

    public ShapedRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        criteria.put(name, criterion);
        return this;
    }

    public ShapedRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ShapedRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    protected ShapedRecipeBuilder builder() {
        return this;
    }

    @Override
    public FinishedRecipe build() {
        if (id == null) {
            id = BuiltInRegistries.ITEM.getKey(result.getItem());
        }
        return new Result(id, group, pattern, key, result, showNotification);
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
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final ItemStack result;
        private final boolean showNotification;

        Result(ResourceLocation id, String group, List<String> pattern, Map<Character, Ingredient> key, ItemStack result, boolean showNotification) {
            this.id = id;
            this.group = group;
            this.pattern = pattern;
            this.key = key;
            this.result = result;
            this.showNotification = showNotification;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }
            JsonArray patternArray = new JsonArray();
            for (String line : pattern) {
                patternArray.add(line);
            }
            json.add("pattern", patternArray);

            JsonObject keyObj = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
                keyObj.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", keyObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            if (result.getCount() > 1) {
                resultObj.addProperty("count", result.getCount());
            }
            json.add("result", resultObj);

            json.addProperty("show_notification", showNotification);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPED_RECIPE;
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
