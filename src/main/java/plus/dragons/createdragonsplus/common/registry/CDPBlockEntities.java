/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.common.fluids.hatch.FluidHatchBlockEntity;

public class CDPBlockEntities {
    public static final BlockEntityEntry<FluidHatchBlockEntity> FLUID_HATCH = REGISTRATE
            .blockEntity("fluid_hatch", FluidHatchBlockEntity::new)
            .validBlocks(CDPBlocks.FLUID_HATCH)
            .renderer(() -> SmartBlockEntityRenderer::new)
            .register();

    public static void register(IEventBus modBus) {
        // Force class loading
    }
}
