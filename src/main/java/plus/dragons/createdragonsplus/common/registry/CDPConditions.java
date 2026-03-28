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

package plus.dragons.createdragonsplus.common.registry;

import net.minecraftforge.common.crafting.CraftingHelper;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.config.ConfigFeatureCondition;

public class CDPConditions {
    public static void register() {
        CraftingHelper.register(ConfigFeatureCondition.Serializer.INSTANCE);

        // Register all config features so they can be looked up by name
        // Primary names (camelCase) and aliases (from original CDPFeaturesConfig paths/aliases)
        ConfigFeatureCondition.registerFeature("dyeFluids", CDPConfig.common().dyeFluids::get);
        ConfigFeatureCondition.registerFeature("dye_fluids", CDPConfig.common().dyeFluids::get);
        ConfigFeatureCondition.registerFeature("fluid/dye", CDPConfig.common().dyeFluids::get);

        ConfigFeatureCondition.registerFeature("dragonBreathFluid", CDPConfig.common().dragonBreathFluid::get);
        ConfigFeatureCondition.registerFeature("dragons_breath_fluid", CDPConfig.common().dragonBreathFluid::get);
        ConfigFeatureCondition.registerFeature("fluid/dragon_breath", CDPConfig.common().dragonBreathFluid::get);

        ConfigFeatureCondition.registerFeature("dyeFluidsLavaInteractionGenerateColoredConcrete",
                CDPConfig.common().dyeFluidsLavaInteractionGenerateColoredConcrete::get);
        ConfigFeatureCondition.registerFeature("fluid/dye/lava_interaction_generate_colored_concrete",
                CDPConfig.common().dyeFluidsLavaInteractionGenerateColoredConcrete::get);

        ConfigFeatureCondition.registerFeature("fluidHatch", CDPConfig.common().fluidHatch::get);
        ConfigFeatureCondition.registerFeature("fluid_hatch", CDPConfig.common().fluidHatch::get);
        ConfigFeatureCondition.registerFeature("block/fluid_hatch", CDPConfig.common().fluidHatch::get);

        ConfigFeatureCondition.registerFeature("blazeUpgradeSmithingTemplate",
                CDPConfig.common().blazeUpgradeSmithingTemplate::get);
        ConfigFeatureCondition.registerFeature("blaze_upgrade_smithing_template",
                CDPConfig.common().blazeUpgradeSmithingTemplate::get);
        ConfigFeatureCondition.registerFeature("item/blaze_upgrade_smithing_template",
                CDPConfig.common().blazeUpgradeSmithingTemplate::get);

        ConfigFeatureCondition.registerFeature("generateAutomaticBrewingRecipeForDragonBreathFluid",
                CDPConfig.common().generateAutomaticBrewingRecipeForDragonBreathFluid::get);
        ConfigFeatureCondition.registerFeature("recipe/automatic_brewing/dragon_breath",
                CDPConfig.common().generateAutomaticBrewingRecipeForDragonBreathFluid::get);

        ConfigFeatureCondition.registerFeature("generateSandPaperPolishingRecipeForPolishedBlocks",
                CDPConfig.common().generateSandPaperPolishingRecipeForPolishedBlocks::get);
        ConfigFeatureCondition.registerFeature("recipe/sand_paper_polishing/polished_blocks",
                CDPConfig.common().generateSandPaperPolishingRecipeForPolishedBlocks::get);

        ConfigFeatureCondition.registerFeature("generateSandPaperPolishingRecipeForOxidizedBlocks",
                CDPConfig.common().generateSandPaperPolishingRecipeForOxidizedBlocks::get);
        ConfigFeatureCondition.registerFeature("recipe/sand_paper_polishing/oxidized_blocks",
                CDPConfig.common().generateSandPaperPolishingRecipeForOxidizedBlocks::get);

        ConfigFeatureCondition.registerFeature("generateSandPaperPolishingRecipeForWaxedBlocks",
                CDPConfig.common().generateSandPaperPolishingRecipeForWaxedBlocks::get);
        ConfigFeatureCondition.registerFeature("recipe/sand_paper_polishing/waxed_blocks",
                CDPConfig.common().generateSandPaperPolishingRecipeForWaxedBlocks::get);
    }
}
