/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package plus.dragons.createdragonsplus.mixin.minecraft;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createdragonsplus.common.registry.CDPCriterions;

/**
 * Mixin to hook into ServerPlayer.awardStat to trigger StatTrigger.
 * In upstream NeoForge, this is handled by StatAwardEvent on the EVENT_BUS,
 * but Forge 1.20.1 does not have such an event.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "awardStat", at = @At("TAIL"))
    private void createdragonsplus$onAwardStat(Stat<?> stat, int amount, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        int newValue = self.getStats().getValue(stat);
        CDPCriterions.STAT.trigger(self, stat, newValue);
    }
}
