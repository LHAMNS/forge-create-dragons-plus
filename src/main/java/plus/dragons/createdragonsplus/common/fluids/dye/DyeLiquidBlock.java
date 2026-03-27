/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.fluids.dye;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import java.util.function.Supplier;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;

public class DyeLiquidBlock extends LiquidBlock {
    private final DyeColor color;

    public DyeLiquidBlock(DyeColor color, Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
        this.color = color;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        var type = CDPFanProcessingTypes.COLORING.get(this.color);
        if (entity instanceof ItemEntity itemEntity) {
            FanProcessing.applyProcessing(itemEntity, type);
        } else if (entity instanceof LivingEntity livingEntity) {
            type.applyColoring(livingEntity, level);
        }
    }
}
