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

package plus.dragons.createdragonsplus.data.recipe.integration;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.recipe.BaseRecipeBuilder;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Greatly simplified -- in 1.20.1 we don't need the complex Codec-based serializer wrapping
 * or MappedRegistryAccessor hack. Instead, we wrap a FinishedRecipe and override the result
 * serialization to use the integration ResourceLocation.
 */
public final class IntegrationResultRecipe {

    private IntegrationResultRecipe() {}

    /**
     * A FinishedRecipe wrapper that replaces the result with an integration result
     * (an item from another mod referenced by ResourceLocation).
     */
    static class IntegrationFinishedRecipe implements FinishedRecipe {
        private final FinishedRecipe delegate;
        private final IntegrationResult result;

        IntegrationFinishedRecipe(FinishedRecipe delegate, IntegrationResult result) {
            this.delegate = delegate;
            this.result = result;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            delegate.serializeRecipeData(json);
            // Override the result field with integration result
            json.add("result", result.toJson());
        }

        @Override
        public ResourceLocation getId() {
            return delegate.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return delegate.getType();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return delegate.serializeAdvancement();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return delegate.getAdvancementId();
        }
    }

    /**
     * Builder that wraps another recipe builder and replaces its result
     * with an integration result pointing to an item from another mod.
     */
    public static class Builder extends BaseRecipeBuilder<net.minecraft.world.item.crafting.Recipe<?>, Builder> {
        private final BaseRecipeBuilder<?, ?> delegate;
        private final ItemStack delegateResult;
        private final ResourceLocation result;

        public Builder(BaseRecipeBuilder<?, ?> delegate, ItemStack delegateResult, ResourceLocation result) {
            super(delegate.getDirectory());
            this.delegate = delegate;
            this.delegateResult = delegateResult;
            this.result = result;
            if (delegate.getId() == null) {
                delegate.withId(result);
            }
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public FinishedRecipe build() {
            var finished = delegate.build();
            var integrationResult = new IntegrationResult(delegateResult, result);
            return new IntegrationFinishedRecipe(finished, integrationResult);
        }

        @Override
        public @Nullable Advancement.Builder buildAdvancement() {
            return delegate.buildAdvancement();
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return delegate.getId();
        }

        @Override
        public @Nullable String getDirectory() {
            return delegate.getDirectory();
        }

        @Override
        public Builder withId(ResourceLocation id) {
            delegate.withId(id);
            return this;
        }

        @Override
        public Builder withCondition(ICondition condition) {
            delegate.withCondition(condition);
            return this;
        }
    }
}
