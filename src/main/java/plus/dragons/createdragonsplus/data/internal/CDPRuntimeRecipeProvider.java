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

package plus.dragons.createdragonsplus.data.internal;

import java.util.List;
import java.util.function.Consumer;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingRecipe;
import plus.dragons.createdragonsplus.common.recipe.UpdateRecipesEvent;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Key changes:
 * - RecipeOutput -> Consumer<FinishedRecipe>
 * - RecipeProvider constructor: no CompletableFuture<Provider>
 * - No DataMapHooks (NeoForge-only) -- oxidized/waxed block recipes use Forge equivalents
 * - @EventBusSubscriber -> manual registration or @Mod.EventBusSubscriber
 */
@Mod.EventBusSubscriber(modid = CDPCommon.ID)
public class CDPRuntimeRecipeProvider extends RecipeProvider {
    public CDPRuntimeRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        if (CDPConfig.COMMON.generateSandPaperPolishingRecipeForPolishedBlocks.get()) {
            buildPolishedBlockRecipes(output);
        }
    }

    private static void buildPolishedBlockRecipes(Consumer<FinishedRecipe> output) {
        BuiltInRegistries.BLOCK.holders()
                .filter(holder -> holder.key().location().getPath().contains("polished_"))
                .forEach(holder -> {
                    var polishedId = holder.key().location();
                    var baseId = polishedId.withPath(name -> name.replace("polished_", ""));
                    if (!BuiltInRegistries.BLOCK.containsKey(baseId))
                        return;
                    var polishedItem = holder.value().asItem();
                    var baseBlockOpt = BuiltInRegistries.BLOCK.getOptional(baseId);
                    if (baseBlockOpt.isEmpty() || baseBlockOpt.get().builtInRegistryHolder().is(CDPBlocks.NOT_APPLICABLE_POLISHING))
                        return;
                    var baseItem = baseBlockOpt.get().asItem();
                    if (polishedItem == Items.AIR || baseItem == Items.AIR)
                        return;
                    var recipeId = CDPCommon.asResource(baseId.toString().replace(':', '/'));
                    CreateRecipeBuilders.polishing(recipeId)
                            .require(baseItem)
                            .output(polishedItem)
                            .build(output);
                    var sandingId = CDPCommon.asResource(baseId.toString().replace(':', '/') + "_sanding");
                    CreateRecipeBuilders.sanding(sandingId)
                            .require(baseItem)
                            .output(polishedItem)
                            .build(output);
                });
    }

    /**
     * Generates oxidized and waxed block sandpaper polishing recipes at runtime.
     * Uses WeatheringCopper.PREVIOUS_BY_BLOCK for oxidized blocks and
     * HoneycombItem.WAX_OFF_BY_BLOCK for waxed blocks, matching the original's
     * DataMapHooks.INVERSE_OXIDIZABLES_DATAMAP and INVERSE_WAXABLES_DATAMAP.
     */
    @SubscribeEvent
    public static void buildRecipesForUpdate(final UpdateRecipesEvent event) {
        // Clear tag-based caches since tag membership may have changed on reload
        CDPDataMaps.clearTagCaches();
        if (CDPConfig.COMMON.generateSandPaperPolishingRecipeForOxidizedBlocks.get()) {
            buildOxidizedBlockRecipes(event);
        }
        if (CDPConfig.COMMON.generateSandPaperPolishingRecipeForWaxedBlocks.get()) {
            buildWaxedBlockRecipes(event);
        }
        if (CDPConfig.COMMON.generateAutomaticBrewingRecipeForDragonBreathFluid.get()) {
            buildDragonBreathFluidRecipes(event);
        }
        buildSandingFromPolishingRecipes(event);
    }

    private static void buildOxidizedBlockRecipes(UpdateRecipesEvent event) {
        // WeatheringCopper.PREVIOUS_BY_BLOCK maps oxidized -> previous (inverse of NEXT_BY_BLOCK)
        WeatheringCopper.PREVIOUS_BY_BLOCK.get().forEach((oxidized, previous) -> {
            var oxidizedItem = oxidized.asItem();
            var previousItem = previous.asItem();
            if (oxidizedItem == Items.AIR || previousItem == Items.AIR)
                return;
            var oxidizedId = BuiltInRegistries.BLOCK.getKey(oxidized);
            var recipeId = CDPCommon.asResource(oxidizedId.toString().replace(':', '/'));
            Recipe<?> recipe = CreateRecipeBuilders.polishing(recipeId)
                    .require(oxidizedItem)
                    .output(previousItem)
                    .build();
            event.addRecipe(recipeId, recipe);
            var sandingId = CDPCommon.asResource(oxidizedId.toString().replace(':', '/') + "_sanding");
            Recipe<?> sandingRecipe = CreateRecipeBuilders.sanding(sandingId)
                    .require(oxidizedItem)
                    .output(previousItem)
                    .build();
            event.addRecipe(sandingId, sandingRecipe);
        });
    }

    private static void buildWaxedBlockRecipes(UpdateRecipesEvent event) {
        // HoneycombItem.WAX_OFF_BY_BLOCK maps waxed -> unwaxed (inverse of WAXABLES)
        HoneycombItem.WAX_OFF_BY_BLOCK.get().forEach((waxed, unwaxed) -> {
            var waxedItem = waxed.asItem();
            var unwaxedItem = unwaxed.asItem();
            if (waxedItem == Items.AIR || unwaxedItem == Items.AIR)
                return;
            var waxedId = BuiltInRegistries.BLOCK.getKey(waxed);
            var recipeId = CDPCommon.asResource(waxedId.toString().replace(':', '/'));
            Recipe<?> recipe = CreateRecipeBuilders.polishing(recipeId)
                    .require(waxedItem)
                    .output(unwaxedItem)
                    .build();
            event.addRecipe(recipeId, recipe);
            var sandingId = CDPCommon.asResource(waxedId.toString().replace(':', '/') + "_sanding");
            Recipe<?> sandingRecipe = CreateRecipeBuilders.sanding(sandingId)
                    .require(waxedItem)
                    .output(unwaxedItem)
                    .build();
            event.addRecipe(sandingId, sandingRecipe);
        });
    }

    private static void buildDragonBreathFluidRecipes(UpdateRecipesEvent event) {
        List<MixingRecipe> dragonBreathRecipes = PotionMixingRecipes.BY_ITEM.get(Items.DRAGON_BREATH);
        if (dragonBreathRecipes == null || dragonBreathRecipes.isEmpty()) {
            return;
        }
        for (MixingRecipe originalRecipe : dragonBreathRecipes) {
            // Get the original recipe's ingredients and output
            var ingredients = originalRecipe.getIngredients();
            var fluidIngredients = originalRecipe.getFluidIngredients();
            var fluidResults = originalRecipe.getFluidResults();
            if (fluidIngredients.isEmpty() || fluidResults.isEmpty())
                continue;
            var fromFluid = fluidIngredients.get(0).getMatchingFluidStacks().get(0);
            var toFluid = fluidResults.get(0);
            // Find the potion ingredient (non-dragon breath)
            Ingredient potionIngredient = null;
            for (var ingredient : ingredients) {
                if (!ingredient.test(new net.minecraft.world.item.ItemStack(Items.DRAGON_BREATH))) {
                    potionIngredient = ingredient;
                    break;
                }
            }
            if (potionIngredient == null)
                continue;
            // Build the dragon breath fluid version of the recipe
            var recipeId = CDPCommon.asResource(originalRecipe.getId().getPath() + "_using_dragon_breath_fluid");
            var builder = new ProcessingRecipeBuilder<>(MixingRecipe::new, recipeId)
                    .require(CDPFluids.COMMON_TAGS.dragonBreath, 250)
                    .require(FluidIngredient.fromFluidStack(fromFluid))
                    .require(potionIngredient)
                    .output(toFluid)
                    .requiresHeat(HeatCondition.HEATED);
            var recipe = builder.build();
            event.addRecipe(recipeId, recipe);
        }
    }

    /**
     * Converts all registered {@code SANDPAPER_POLISHING} recipes into corresponding
     * {@code SANDING} recipes, so that the attribute filter and
     * {@link plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingFanProcessingType#canProcess}
     * can detect "sanding-capable" items that only have sandpaper polishing recipes
     * (e.g., Rose Quartz → Polished Rose Quartz).
     */
    @SuppressWarnings("unchecked")
    private static void buildSandingFromPolishingRecipes(UpdateRecipesEvent event) {
        event.getRecipesForType(AllRecipeTypes.SANDPAPER_POLISHING.<RecipeType<SandPaperPolishingRecipe>>getType())
                .stream()
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED)
                .map(SandingRecipe::convertSandPaperPolishing)
                .forEach(recipe -> event.addRecipe(recipe.getId(), recipe));
    }

}
