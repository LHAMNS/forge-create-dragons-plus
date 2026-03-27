/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data.internal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createdragonsplus.integration.ModIntegration;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * NeoForge's RegistrateDataMapProvider does not exist in Forge 1.20.1.
 * Data maps are a NeoForge-only feature. In Forge 1.20.1, the equivalent
 * data is registered programmatically through CDPDataMaps instead.
 *
 * In the upstream NeoForge version, the DataMap maps individual fluid
 * ResourceLocations (not tags) to DyeColor. This Forge port registers
 * individual Fluid instances looked up from the registry by ResourceLocation.
 */
public class CDPRegistrateDataMaps {
    /**
     * Registers fan coloring catalyst fluids from Create: Garnished integration.
     * In Forge 1.20.1, this is called programmatically instead of through datagen.
     *
     * Upstream NeoForge registers these as individual fluid entries in a DataMap,
     * so here we register by individual Fluid lookup rather than by tag.
     */
    public static void registerGarnishedColoringCatalysts() {
        for (var color : DyeColors.ALL) {
            ResourceLocation stillId = ModIntegration.CREATE_GARNISHED.asResource(color.getSerializedName() + "_mastic_resin");
            ResourceLocation flowingId = new ResourceLocation(stillId.getNamespace(), "flowing_" + stillId.getPath());
            BuiltInRegistries.FLUID.getOptional(stillId).ifPresent(fluid ->
                    CDPDataMaps.registerFluidCatalyst(fluid, color));
            BuiltInRegistries.FLUID.getOptional(flowingId).ifPresent(fluid ->
                    CDPDataMaps.registerFluidCatalyst(fluid, color));
        }
    }
}
