/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.test;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;

/**
 * Runtime registry and API integration tests for Create: Dragons Plus.
 * These tests verify that all registered game objects are present and accessible
 * at runtime, catching issues like missing registrations, broken references,
 * or config/data initialization failures.
 */
@GameTestHolder(CDPCommon.ID)
public class CDPRegistryIntegrationTest {

    /**
     * Verify all 16 dye fluids are registered and resolvable from the Forge fluid registry.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testAllDyeFluidsRegistered(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            if (!CDPFluids.DYES_BY_COLOR.containsKey(color)) {
                missing.add(color.getName());
                continue;
            }
            Fluid fluid = CDPFluids.DYES_BY_COLOR.get(color).get();
            if (fluid == null) {
                missing.add(color.getName() + " (null fluid)");
                continue;
            }
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
            if (id == null || id.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
                missing.add(color.getName() + " (unregistered)");
            }
        }
        if (!missing.isEmpty()) {
            helper.fail("Missing dye fluids: " + String.join(", ", missing));
            return;
        }
        helper.succeed();
    }

    /**
     * Verify Dragon's Breath fluid is registered.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testDragonBreathFluidRegistered(GameTestHelper helper) {
        Fluid fluid = CDPFluids.DRAGON_BREATH.get();
        if (fluid == null) {
            helper.fail("Dragon's Breath fluid is null");
            return;
        }
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) {
            helper.fail("Dragon's Breath fluid is not in Forge registry");
            return;
        }
        helper.succeed();
    }

    /**
     * Verify the Fluid Hatch block is registered and has a valid block item.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testFluidHatchBlockRegistered(GameTestHelper helper) {
        Block block = CDPBlocks.FLUID_HATCH.get();
        if (block == null) {
            helper.fail("Fluid Hatch block is null");
            return;
        }
        Item item = block.asItem();
        if (item == Items.AIR) {
            helper.fail("Fluid Hatch block has no block item");
            return;
        }
        helper.succeed();
    }

    /**
     * Verify all 4 fan processing types are registered in Create's registry.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testFanProcessingTypesRegistered(GameTestHelper helper) {
        CDPFanProcessingTypes.ensureRegistered();

        List<String> failures = new ArrayList<>();

        if (CDPFanProcessingTypes.FREEZING == null) {
            failures.add("FREEZING is null");
        }
        if (CDPFanProcessingTypes.SANDING == null) {
            failures.add("SANDING is null");
        }
        if (CDPFanProcessingTypes.ENDING == null) {
            failures.add("ENDING is null");
        }
        if (CDPFanProcessingTypes.COLORING == null || CDPFanProcessingTypes.COLORING.isEmpty()) {
            failures.add("COLORING map is null or empty");
        } else if (CDPFanProcessingTypes.COLORING.size() != 16) {
            failures.add("COLORING map has " + CDPFanProcessingTypes.COLORING.size() + " entries, expected 16");
        }

        if (!failures.isEmpty()) {
            helper.fail("Fan processing type failures: " + String.join(", ", failures));
            return;
        }
        helper.succeed();
    }

    /**
     * Verify all 4 recipe types are registered.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testRecipeTypesRegistered(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();

        if (CDPRecipes.COLORING.getType() == null)
            failures.add("COLORING recipe type null");
        if (CDPRecipes.FREEZING.getType() == null)
            failures.add("FREEZING recipe type null");
        if (CDPRecipes.SANDING.getType() == null)
            failures.add("SANDING recipe type null");
        if (CDPRecipes.ENDING.getType() == null)
            failures.add("ENDING recipe type null");

        if (!failures.isEmpty()) {
            helper.fail(String.join(", ", failures));
            return;
        }
        helper.succeed();
    }

    /**
     * Verify CDPDataMaps coloring catalyst registration is operational.
     * At minimum, dye fluids should map to their respective DyeColors.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testDataMapColoringCatalysts(GameTestHelper helper) {
        Map<?, DyeColor> fluidCatalysts = CDPDataMaps.getFluidFanColoringCatalysts();
        if (fluidCatalysts == null) {
            helper.fail("Fluid coloring catalyst map is null");
            return;
        }
        if (fluidCatalysts.isEmpty()) {
            helper.fail("Fluid coloring catalyst map is empty — dye fluids not registered as catalysts");
            return;
        }
        helper.succeed();
    }

    /**
     * Verify CDPConfig instances are non-null and accessible.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testConfigAccessible(GameTestHelper helper) {
        if (CDPConfig.common() == null) {
            helper.fail("CDPConfig.common() is null");
            return;
        }
        if (CDPConfig.server() == null) {
            helper.fail("CDPConfig.server() is null");
            return;
        }
        helper.succeed();
    }

    /**
     * Verify the Blaze Upgrade Smithing Template item is registered.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testBlazeUpgradeTemplateRegistered(GameTestHelper helper) {
        Item template = CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get();
        if (template == null) {
            helper.fail("Blaze Upgrade Smithing Template item is null");
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(template);
        if (id == null) {
            helper.fail("Blaze Upgrade Smithing Template not in Forge registry");
            return;
        }
        helper.succeed();
    }

    /**
     * Verify dye bucket items exist for all 16 colors.
     */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testDyeBucketItemsExist(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation bucketId = new ResourceLocation(CDPCommon.ID, color.getName() + "_dye_bucket");
            Item bucket = ForgeRegistries.ITEMS.getValue(bucketId);
            if (bucket == null || bucket == Items.AIR) {
                missing.add(color.getName());
            }
        }
        if (!missing.isEmpty()) {
            helper.fail("Missing dye bucket items: " + String.join(", ", missing));
            return;
        }
        helper.succeed();
    }
}
