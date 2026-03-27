/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class CDPConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CDPCommonConfig COMMON;
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final CDPServerConfig SERVER;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final CDPClientConfig CLIENT;

    static {
        Pair<CDPCommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CDPCommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<CDPServerConfig, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(CDPServerConfig::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();

        Pair<CDPClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(CDPClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static CDPCommonConfig common() { return COMMON; }
    public static CDPServerConfig server() { return SERVER; }
    public static CDPClientConfig client() { return CLIENT; }
}
