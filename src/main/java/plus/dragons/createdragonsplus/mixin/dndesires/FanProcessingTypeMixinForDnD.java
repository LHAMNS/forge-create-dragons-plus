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

package plus.dragons.createdragonsplus.mixin.dndesires;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.ModIntegration;

import java.util.List;

/**
 * Disables DnD's fan processing types when CDP's equivalents are active.
 * <p>
 * Cannot mixin directly into FanProcessingType (it's an interface in Create 6.0.8).
 * Instead, hooks into FanProcessingTypeRegistry.init() to remove conflicting types
 * from the mutable SORTED_TYPES list after all types are registered and sorted.
 * <p>
 * Version note: On Forge 1.20.1, DnDesires uses package "uwu.lopyluna.create_dd".
 * The rename to "dev.lopyluna.dndesires" only applies to NeoForge 1.21.1+.
 */
@Restriction(require = @Condition(ModIntegration.Constants.CREATE_DND))
@Mixin(value = FanProcessingTypeRegistry.class, remap = false)
public abstract class FanProcessingTypeMixinForDnD {
    @Shadow
    private static List<FanProcessingType> SORTED_TYPES;

    @Inject(method = "init", at = @At("TAIL"))
    private static void cdp$removeDnDConflicts(CallbackInfo ci) {
        SORTED_TYPES.removeIf(type -> {
            String className = type.getClass().getName();
            // DnD 0.2c FreezingType
            if (className.equals("uwu.lopyluna.create_dd.block.BlockProperties.industrial_fan.Processing.IndustrialTypeFanProcessing$FreezingType")
                    && CDPConfig.server().enableBulkFreezing.get())
                return true;
            // Forward compatibility: DragonBreathing if/when DnD adds it
            if (className.contains("DragonBreathingType") && CDPConfig.server().enableBulkEnding.get())
                return true;
            // Forward compatibility: Sanding if/when DnD adds it
            if (className.contains("SandingType") && CDPConfig.server().enableBulkSanding.get())
                return true;
            return false;
        });
    }
}
