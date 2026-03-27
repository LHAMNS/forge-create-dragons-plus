/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CDPCommonConfig {
    public final ForgeConfigSpec.BooleanValue dyeFluids;
    public final ForgeConfigSpec.BooleanValue dragonBreathFluid;
    public final ForgeConfigSpec.BooleanValue dyeFluidsLavaInteractionGenerateColoredConcrete;
    public final ForgeConfigSpec.BooleanValue fluidHatch;
    public final ForgeConfigSpec.BooleanValue blazeUpgradeSmithingTemplate;
    public final ForgeConfigSpec.BooleanValue generateAutomaticBrewingRecipeForDragonBreathFluid;
    public final ForgeConfigSpec.BooleanValue generateSandPaperPolishingRecipeForPolishedBlocks;
    public final ForgeConfigSpec.BooleanValue generateSandPaperPolishingRecipeForOxidizedBlocks;
    public final ForgeConfigSpec.BooleanValue generateSandPaperPolishingRecipeForWaxedBlocks;

    public CDPCommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("features");
        dyeFluids = builder.comment("Enable dye fluids").define("dyeFluids", true);
        dragonBreathFluid = builder.comment("Enable dragon breath fluid").define("dragonBreathFluid", true);
        dyeFluidsLavaInteractionGenerateColoredConcrete = builder.comment("Dye fluids + lava = colored concrete")
                .define("dyeFluidsLavaInteractionGenerateColoredConcrete", true);
        fluidHatch = builder.comment("Enable fluid hatch block").define("fluidHatch", true);
        blazeUpgradeSmithingTemplate = builder.comment("Enable blaze upgrade smithing template")
                .define("blazeUpgradeSmithingTemplate", false);
        builder.pop();

        builder.push("recipes");
        generateAutomaticBrewingRecipeForDragonBreathFluid = builder
                .define("generateAutomaticBrewingRecipeForDragonBreathFluid", true);
        generateSandPaperPolishingRecipeForPolishedBlocks = builder
                .define("generateSandPaperPolishingRecipeForPolishedBlocks", true);
        generateSandPaperPolishingRecipeForOxidizedBlocks = builder
                .define("generateSandPaperPolishingRecipeForOxidizedBlocks", true);
        generateSandPaperPolishingRecipeForWaxedBlocks = builder
                .define("generateSandPaperPolishingRecipeForWaxedBlocks", true);
        builder.pop();
    }
}
