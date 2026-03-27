/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.mixin.minecraft;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Shulker.class)
public interface ShulkerAccessor {
    @Accessor("DATA_COLOR_ID")
    static EntityDataAccessor<Byte> getDataColorId() {
        throw new AssertionError();
    }
}
