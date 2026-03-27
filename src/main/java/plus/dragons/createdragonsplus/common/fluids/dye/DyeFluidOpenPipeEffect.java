/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.fluids.dye;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;

public class DyeFluidOpenPipeEffect implements OpenPipeEffectHandler {
    private final DyeColor color;

    public DyeFluidOpenPipeEffect(DyeColor color) {
        this.color = color;
    }

    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        var type = CDPFanProcessingTypes.COLORING.get(this.color);
        var entities = level.getEntities((Entity) null, area,
                entity -> entity instanceof ItemEntity || entity instanceof LivingEntity);
        for (var entity : entities) {
            if (entity instanceof ItemEntity itemEntity) {
                FanProcessing.applyProcessing(itemEntity, type);
            } else if (entity instanceof LivingEntity livingEntity) {
                type.applyColoring(livingEntity, level);
            }
        }
    }
}
