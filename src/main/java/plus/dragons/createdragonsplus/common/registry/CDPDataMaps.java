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

package plus.dragons.createdragonsplus.common.registry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.data.internal.CDPRegistrateDataMaps;
import plus.dragons.createdragonsplus.integration.ModIntegration;

/**
 * Replaces NeoForge DataMap API for Forge 1.20.1.
 * Provides static maps for fan processing catalyst coloring associations.
 * Supports both tag-based and individual block/fluid catalyst lookups,
 * matching the original NeoForge DataMapType behavior.
 */
public class CDPDataMaps {
    /** Map from fluid tag to DyeColor for fan coloring catalysts. */
    private static final Map<TagKey<Fluid>, DyeColor> FLUID_FAN_COLORING_CATALYSTS = new HashMap<>();

    /** Map from block tag to DyeColor for fan coloring catalysts. */
    private static final Map<TagKey<Block>, DyeColor> BLOCK_TAG_FAN_COLORING_CATALYSTS = new HashMap<>();

    /** Map from individual fluid to DyeColor for fan coloring catalysts. */
    private static final Map<Fluid, DyeColor> FLUID_DIRECT_FAN_COLORING_CATALYSTS = new HashMap<>();

    /** Map from individual block to DyeColor for fan coloring catalysts. */
    private static final Map<Block, DyeColor> BLOCK_FAN_COLORING_CATALYSTS = new HashMap<>();

    /** Map from DyeColor to its corresponding fluid tag. */
    private static final EnumMap<DyeColor, TagKey<Fluid>> FLUID_TAGS_BY_COLOR = new EnumMap<>(DyeColor.class);

    /** Map from DyeColor to its corresponding block tag. */
    private static final EnumMap<DyeColor, TagKey<Block>> BLOCK_TAGS_BY_COLOR = new EnumMap<>(DyeColor.class);

    /**
     * Result cache: maps a resolved Fluid to its DyeColor after a tag scan hit.
     * Avoids repeated iteration over tag maps on the hot path (called per ColoringFanProcessingType).
     * Cleared when new catalysts are registered, since tag membership may change.
     */
    private static final Map<Fluid, DyeColor> FLUID_TAG_RESULT_CACHE = new ConcurrentHashMap<>();

    /**
     * Result cache: maps a resolved Block to its DyeColor after a tag scan hit.
     * Cleared when new catalysts are registered.
     */
    private static final Map<Block, DyeColor> BLOCK_TAG_RESULT_CACHE = new ConcurrentHashMap<>();

    public static void register() {
        // Register fluid tag -> DyeColor mappings
        for (DyeColor color : DyeColors.ALL) {
            TagKey<Fluid> fluidTag = CDPFluids.COMMON_TAGS.dyesByColor.get(color);
            if (fluidTag != null) {
                FLUID_FAN_COLORING_CATALYSTS.put(fluidTag, color);
                FLUID_TAGS_BY_COLOR.put(color, fluidTag);
            }
        }

        // Register block -> DyeColor mappings for concrete powder blocks
        for (DyeColor color : DyeColors.ALL) {
            Block concretePowder = BuiltInRegistries.BLOCK
                    .getOptional(new ResourceLocation(color.getName() + "_concrete_powder"))
                    .orElse(null);
            if (concretePowder != null) {
                BLOCK_FAN_COLORING_CATALYSTS.put(concretePowder, color);
            }
        }

        // Garnished fluid catalysts are registered later during FMLCommonSetupEvent
        // to ensure Garnished fluids are fully registered by that point.
        // See CDPCommon.setup() for the deferred registration.
    }

    /**
     * Checks a fluid state against all registered fluid coloring catalysts.
     * @return the DyeColor if the fluid is a coloring catalyst, or null
     */
    public static DyeColor getFluidColoringCatalyst(FluidState fluidState) {
        if (fluidState.isEmpty()) return null;
        Fluid fluid = fluidState.getType();
        // Check individual fluid mappings first (fast path for Garnished etc.)
        DyeColor color = FLUID_DIRECT_FAN_COLORING_CATALYSTS.get(fluid);
        if (color != null) return color;
        // Check result cache (avoids repeated tag iteration on hot path)
        color = FLUID_TAG_RESULT_CACHE.get(fluid);
        if (color != null) return color;
        // Check tag-based mappings and cache the result
        for (var entry : FLUID_FAN_COLORING_CATALYSTS.entrySet()) {
            if (fluidState.is(entry.getKey())) {
                FLUID_TAG_RESULT_CACHE.put(fluid, entry.getValue());
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Checks a block state against all registered block coloring catalysts.
     * Checks both individual blocks and block tags.
     * @return the DyeColor if the block is a coloring catalyst, or null
     */
    public static DyeColor getBlockColoringCatalyst(BlockState blockState) {
        Block block = blockState.getBlock();
        // Check individual block mappings first (fast path)
        DyeColor color = BLOCK_FAN_COLORING_CATALYSTS.get(block);
        if (color != null) return color;
        // Check result cache (avoids repeated tag iteration on hot path)
        color = BLOCK_TAG_RESULT_CACHE.get(block);
        if (color != null) return color;
        // Check tag-based mappings and cache the result
        for (var entry : BLOCK_TAG_FAN_COLORING_CATALYSTS.entrySet()) {
            if (blockState.is(entry.getKey())) {
                BLOCK_TAG_RESULT_CACHE.put(block, entry.getValue());
                return entry.getValue();
            }
        }
        return null;
    }

    public static DyeColor getFluidFanColoringCatalyst(TagKey<Fluid> tag) {
        return FLUID_FAN_COLORING_CATALYSTS.get(tag);
    }

    public static DyeColor getBlockFanColoringCatalyst(TagKey<Block> tag) {
        return BLOCK_TAG_FAN_COLORING_CATALYSTS.get(tag);
    }

    public static Map<TagKey<Fluid>, DyeColor> getFluidFanColoringCatalysts() {
        return FLUID_FAN_COLORING_CATALYSTS;
    }

    public static Map<TagKey<Block>, DyeColor> getBlockFanColoringCatalysts() {
        return BLOCK_TAG_FAN_COLORING_CATALYSTS;
    }

    public static Map<Block, DyeColor> getBlockDirectFanColoringCatalysts() {
        return BLOCK_FAN_COLORING_CATALYSTS;
    }

    public static void registerFluidCatalyst(TagKey<Fluid> tag, DyeColor color) {
        FLUID_FAN_COLORING_CATALYSTS.put(tag, color);
        FLUID_TAGS_BY_COLOR.put(color, tag);
        FLUID_TAG_RESULT_CACHE.clear(); // Invalidate cache since tag mappings changed
    }

    public static void registerBlockCatalyst(TagKey<Block> tag, DyeColor color) {
        BLOCK_TAG_FAN_COLORING_CATALYSTS.put(tag, color);
        BLOCK_TAGS_BY_COLOR.put(color, tag);
        BLOCK_TAG_RESULT_CACHE.clear(); // Invalidate cache since tag mappings changed
    }

    public static void registerFluidCatalyst(Fluid fluid, DyeColor color) {
        FLUID_DIRECT_FAN_COLORING_CATALYSTS.put(fluid, color);
    }

    public static void registerBlockCatalyst(Block block, DyeColor color) {
        BLOCK_FAN_COLORING_CATALYSTS.put(block, color);
    }
}
