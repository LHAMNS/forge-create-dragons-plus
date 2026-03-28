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

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * A utility implementation of Create's {@link IRecipeTypeInfo} that registers both
 * the {@link RecipeSerializer} and {@link RecipeType} via Forge's DeferredRegister.
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- uses RegistryObject instead of DeferredHolder.
 */
@SuppressWarnings("unchecked")
public class RecipeTypeInfo<R extends Recipe<?>> implements IRecipeTypeInfo {
    private final ResourceLocation id;
    private final RegistryObject<RecipeSerializer<?>> serializer;
    private final RegistryObject<RecipeType<?>> type;

    public RecipeTypeInfo(String modid, String name, Supplier<? extends RecipeSerializer<R>> serializer,
                          DeferredRegister<RecipeSerializer<?>> serializerRegister,
                          DeferredRegister<RecipeType<?>> typeRegister) {
        this.id = new ResourceLocation(modid, name);
        this.serializer = serializerRegister.register(name, serializer::get);
        this.type = typeRegister.register(name, () -> RecipeType.simple(this.id));
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializer.get();
    }

    @Override
    public <T extends RecipeType<?>> T getType() {
        return (T) type.get();
    }
}
