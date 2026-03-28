/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
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

package plus.dragons.createdragonsplus.common.fluids.dye;

import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import java.util.function.Supplier;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType;
import plus.dragons.createdragonsplus.config.CDPConfig;

public final class DyeFluidType extends SolidRenderFluidType {
    private final DyeColor color;

    private DyeFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, Vector3f fogColor, Supplier<Float> fogDistanceModifier, DyeColor color) {
        super(properties, stillTexture, flowingTexture, tintColor, fogColor, fogDistanceModifier);
        this.color = color;
    }

    public static FluidTypeFactory create(DyeColor color) {
        // In 1.20.1: getTextureDiffuseColor() -> getFireworkColor()
        int tintColor = 0xFF000000 | (color.getFireworkColor() & 0xFFFFFF);
        Vector3f fogColor = new Color(tintColor).asVectorF();
        return (properties, stillTexture, flowingTexture) -> new DyeFluidType(properties,
                stillTexture,
                flowingTexture,
                tintColor,
                fogColor,
                DyeFluidType::getVisibility,
                color);
    }

    private static float getVisibility() {
        return (float) (CDPConfig.client().dyeVisionMultiplier.get() / 256.0);
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return level.dimensionType().ultraWarm();
    }
}
