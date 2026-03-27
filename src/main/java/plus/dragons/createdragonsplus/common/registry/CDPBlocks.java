/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.hatch.FluidHatchBlock;

public class CDPBlocks {
    public static final TagKey<Block> PASSIVE_BLOCK_FREEZERS = BlockTags.create(new ResourceLocation(CDPCommon.ID, "passive_block_freezers"));
    public static final TagKey<Block> FAN_SANDING_CATALYSTS = BlockTags.create(new ResourceLocation(CDPCommon.ID, "fan_processing_catalysts/sanding"));
    public static final TagKey<Block> FAN_ENDING_CATALYSTS = BlockTags.create(new ResourceLocation(CDPCommon.ID, "fan_processing_catalysts/ending"));
    public static final TagKey<Block> NOT_APPLICABLE_POLISHING = BlockTags.create(new ResourceLocation(CDPCommon.ID, "not_applicable_for_polishing"));

    /** @deprecated Use static fields directly. Kept for API compatibility. */
    @Deprecated
    public static class ModTags {
        public final TagKey<Block> passiveBlockFreezers = PASSIVE_BLOCK_FREEZERS;
        public final TagKey<Block> fanSandingCatalysts = FAN_SANDING_CATALYSTS;
        public final TagKey<Block> fanEndingCatalysts = FAN_ENDING_CATALYSTS;
        public final TagKey<Block> notApplicablePolishing = NOT_APPLICABLE_POLISHING;
    }

    /** @deprecated Use static fields directly. Kept for API compatibility. */
    @Deprecated
    public static final ModTags MOD_TAGS = new ModTags();

    public static final BlockEntry<FluidHatchBlock> FLUID_HATCH = REGISTRATE
            .block("fluid_hatch", FluidHatchBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.get(), AssetLookup.standardModel(ctx, prov)))
            .simpleItem()
            .register();

    public static void register(IEventBus modBus) {
        // Force class loading to trigger static init
    }
}
