/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.registry;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingRecipe;
import plus.dragons.createdragonsplus.common.recipe.RecipeTypeInfo;

import java.util.function.Supplier;

public class CDPRecipes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CDPCommon.ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CDPCommon.ID);

    public static final RecipeTypeInfo<ColoringRecipe> COLORING = register("coloring", () -> new ProcessingRecipeSerializer<>(ColoringRecipe::new));
    public static final RecipeTypeInfo<FreezingRecipe> FREEZING = register("freezing", () -> new ProcessingRecipeSerializer<>(FreezingRecipe::new));
    public static final RecipeTypeInfo<SandingRecipe> SANDING = register("sanding", () -> new ProcessingRecipeSerializer<>(SandingRecipe::new));
    public static final RecipeTypeInfo<EndingRecipe> ENDING = register("ending", () -> new ProcessingRecipeSerializer<>(EndingRecipe::new));

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
    }

    private static <R extends Recipe<?>> RecipeTypeInfo<R> register(String name, Supplier<? extends RecipeSerializer<R>> serializer) {
        return new RecipeTypeInfo<>(CDPCommon.ID, name, serializer, SERIALIZERS, TYPES);
    }
}
