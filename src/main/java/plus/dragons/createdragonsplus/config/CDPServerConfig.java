/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CDPServerConfig {
    public final ForgeConfigSpec.BooleanValue enableBulkColoring;
    public final ForgeConfigSpec.BooleanValue enableBulkFreezing;
    public final ForgeConfigSpec.BooleanValue enableBulkSanding;
    public final ForgeConfigSpec.BooleanValue enableBulkEnding;

    public CDPServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("recipes");
        enableBulkColoring = builder.comment("Enable bulk coloring via fan").define("enableBulkColoring", true);
        enableBulkFreezing = builder.comment("Enable bulk freezing via fan").define("enableBulkFreezing", true);
        enableBulkSanding = builder.comment("Enable bulk sanding via fan").define("enableBulkSanding", true);
        enableBulkEnding = builder.comment("Enable bulk ending via fan").define("enableBulkEnding", true);
        builder.pop();
    }
}
