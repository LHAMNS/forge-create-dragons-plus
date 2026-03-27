/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.registry;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingFanProcessingType;

import java.util.Map;
import java.util.function.Supplier;

public class CDPFanProcessingTypes {
    public static final Map<DyeColor, ColoringFanProcessingType> COLORING;
    public static final FreezingFanProcessingType FREEZING = new FreezingFanProcessingType();
    public static final SandingFanProcessingType SANDING = new SandingFanProcessingType();
    public static final EndingFanProcessingType ENDING = new EndingFanProcessingType();

    static {
        var builder = ImmutableMap.<DyeColor, ColoringFanProcessingType>builder();
        for (var color : DyeColors.ALL) {
            builder.put(color, new ColoringFanProcessingType(color));
        }
        COLORING = builder.build();
    }

    private static boolean registered = false;

    public static void register() {
        registerToCreateRegistry();
    }

    public static void ensureRegistered() {
        if (!registered) {
            registerToCreateRegistry();
        }
    }

    private static void registerToCreateRegistry() {
        if (registered) return;
        registered = true;

        Registry<FanProcessingType> registry = CreateBuiltInRegistries.FAN_PROCESSING_TYPE;

        // Register coloring types directly (one per dye color)
        for (var entry : COLORING.entrySet()) {
            Registry.register(registry, new ResourceLocation(CDPCommon.ID, "coloring_" + entry.getKey().getName()), entry.getValue());
        }

        // Register freezing directly
        Registry.register(registry, new ResourceLocation(CDPCommon.ID, "freezing"), FREEZING);

        // Register sanding directly
        Registry.register(registry, new ResourceLocation(CDPCommon.ID, "sanding"), SANDING);

        // Register ending directly
        Registry.register(registry, new ResourceLocation(CDPCommon.ID, "ending"), ENDING);
    }
}
