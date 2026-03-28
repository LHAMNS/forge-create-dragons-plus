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

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base recipe builder with support for Forge recipe conditions and directory-based output.
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- uses Forge ICondition system,
 * FinishedRecipe/Consumer pattern instead of RecipeOutput/RecipeHolder.
 *
 * @param <R> the recipe type
 * @param <B> the builder self-type for fluent API
 */
public abstract class BaseRecipeBuilder<R extends Recipe<?>, B extends BaseRecipeBuilder<R, ?>> implements Consumer<Consumer<FinishedRecipe>> {
    protected final @Nullable String directory;
    protected final List<ICondition> conditions = new ArrayList<>();
    protected @Nullable ResourceLocation id;

    protected BaseRecipeBuilder(@Nullable String directory) {
        this.directory = directory;
    }

    protected abstract B builder();

    public abstract FinishedRecipe build();

    public @Nullable Advancement.Builder buildAdvancement() {
        return null;
    }

    public @Nullable String getDirectory() {
        return directory;
    }

    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public final void accept(Consumer<FinishedRecipe> output) {
        FinishedRecipe finished = this.build();
        ResourceLocation recipeId = this.id != null ? this.id : finished.getId();
        if (this.directory != null) {
            recipeId = new ResourceLocation(recipeId.getNamespace(), this.directory + "/" + recipeId.getPath());
        }
        // Wrap with conditions if needed
        if (!this.conditions.isEmpty()) {
            output.accept(new ConditionalFinishedRecipe(finished, recipeId, this.conditions));
        } else {
            output.accept(finished);
        }
    }

    public B withId(ResourceLocation id) {
        this.id = id;
        return builder();
    }

    public B withCondition(ICondition condition) {
        this.conditions.add(condition);
        return builder();
    }

    public final B withoutCondition(ICondition condition) {
        this.conditions.add(new NotCondition(condition));
        return builder();
    }

    public final B withAllCondition(ICondition... conditions) {
        Collections.addAll(this.conditions, conditions);
        return builder();
    }

    public final B withAnyCondition(ICondition... conditions) {
        this.conditions.add(new OrCondition(conditions));
        return builder();
    }

    public final B withMod(String mod) {
        this.withCondition(new ModLoadedCondition(mod));
        return builder();
    }

    public final B withoutMod(String mod) {
        this.withoutCondition(new ModLoadedCondition(mod));
        return builder();
    }

    public final B withItem(ResourceLocation location) {
        this.withCondition(new ItemExistsCondition(location));
        return builder();
    }

    public final B withItem(RegistryObject<? extends Item> item) {
        this.withCondition(new ItemExistsCondition(item.getId()));
        return builder();
    }

    public final B withoutItem(ResourceLocation location) {
        this.withoutCondition(new ItemExistsCondition(location));
        return builder();
    }

    public final B withoutItem(RegistryObject<? extends Item> item) {
        this.withoutCondition(new ItemExistsCondition(item.getId()));
        return builder();
    }

    public final B withTag(ResourceLocation location) {
        this.withoutCondition(new TagEmptyCondition(location));
        return builder();
    }

    public final B withTag(TagKey<Item> tag) {
        this.withoutCondition(new TagEmptyCondition(tag.location()));
        return builder();
    }

    public final B withoutTag(ResourceLocation location) {
        this.withCondition(new TagEmptyCondition(location));
        return builder();
    }

    public final B withoutTag(TagKey<Item> tag) {
        this.withCondition(new TagEmptyCondition(tag.location()));
        return builder();
    }

    /**
     * Wrapper that applies Forge recipe conditions to a FinishedRecipe.
     */
    private static class ConditionalFinishedRecipe implements FinishedRecipe {
        private final FinishedRecipe wrapped;
        private final ResourceLocation id;
        private final List<ICondition> conditions;

        ConditionalFinishedRecipe(FinishedRecipe wrapped, ResourceLocation id, List<ICondition> conditions) {
            this.wrapped = wrapped;
            this.id = id;
            this.conditions = conditions;
        }

        @Override
        public void serializeRecipeData(com.google.gson.JsonObject json) {
            wrapped.serializeRecipeData(json);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public net.minecraft.world.item.crafting.RecipeSerializer<?> getType() {
            return wrapped.getType();
        }

        @Override
        public @Nullable com.google.gson.JsonObject serializeAdvancement() {
            return wrapped.serializeAdvancement();
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return wrapped.getAdvancementId();
        }

        @Override
        public com.google.gson.JsonObject serializeRecipe() {
            com.google.gson.JsonObject json = wrapped.serializeRecipe();
            if (!conditions.isEmpty()) {
                com.google.gson.JsonArray conditionsArray = new com.google.gson.JsonArray();
                for (ICondition condition : conditions) {
                    conditionsArray.add(net.minecraftforge.common.crafting.CraftingHelper.serialize(condition));
                }
                json.add("conditions", conditionsArray);
            }
            return json;
        }
    }
}
