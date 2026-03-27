/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.mixin.create;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;

/**
 * Mixin into FanProcessingTypeRegistry to ensure our custom types are
 * registered and included in the sorted types list.
 */
@Mixin(value = FanProcessingTypeRegistry.class, remap = false)
public class FanProcessingTypeMixin {

    @Inject(method = "init", at = @At("HEAD"))
    private static void cdp$ensureRegistered(CallbackInfo ci) {
        CDPFanProcessingTypes.ensureRegistered();
    }
}
