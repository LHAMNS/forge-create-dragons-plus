/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CDPClientConfig {
    public final ForgeConfigSpec.DoubleValue dyeVisionMultiplier;
    public final ForgeConfigSpec.DoubleValue dragonBreathVisionMultiplier;

    public CDPClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("vision");
        dyeVisionMultiplier = builder.comment("The vision range through Dye Fluids will be multiplied by this factor")
                .defineInRange("dyeVisionMultiplier", 1.0, 1.0, 256.0);
        dragonBreathVisionMultiplier = builder.comment("The vision range through Dragon's Breath Fluid will be multiplied by this factor")
                .defineInRange("dragonBreathVisionMultiplier", 1.0, 1.0, 256.0);
        builder.pop();
    }
}
