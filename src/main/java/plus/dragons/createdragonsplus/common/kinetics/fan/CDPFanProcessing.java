/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.kinetics.fan;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;

/**
 * Central lookup for custom fan processing types.
 * Called from the {@link plus.dragons.createdragonsplus.mixin.create.FanProcessingTypeMixin}
 * to determine whether a block position should trigger one of our custom processing types.
 */
public class CDPFanProcessing {

    /**
     * Returns the custom {@link FanProcessingType} for the given position,
     * or {@code null} if no custom type matches.
     */
    @Nullable
    public static FanProcessingType getCustomType(Level level, BlockPos pos) {
        // Check coloring types first (one per dye color)
        for (var entry : CDPFanProcessingTypes.COLORING.entrySet()) {
            if (entry.getValue().isValidAt(level, pos)) {
                return entry.getValue();
            }
        }
        // Freezing
        if (CDPFanProcessingTypes.FREEZING.isValidAt(level, pos)) {
            return CDPFanProcessingTypes.FREEZING;
        }
        // Sanding
        if (CDPFanProcessingTypes.SANDING.isValidAt(level, pos)) {
            return CDPFanProcessingTypes.SANDING;
        }
        // Ending
        if (CDPFanProcessingTypes.ENDING.isValidAt(level, pos)) {
            return CDPFanProcessingTypes.ENDING;
        }
        return null;
    }
}
