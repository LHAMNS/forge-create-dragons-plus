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

import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.recipe.UpdateRecipesEvent;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
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
        });
    }
}
