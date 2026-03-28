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

package plus.dragons.createdragonsplus.mixin.dndesires;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.ModIntegration.Constants;
import uwu.lopyluna.create_dd.jei.fan.DDProcessingViaFanCategory;

/**
 * Disables the DnD Sanding JEI category when CDP's bulk sanding is enabled.
 * <p>
 * Note: DnD 0.2c does not have FanSandingCategory in the jei.fan package.
 * (0.1b had it at uwu.lopyluna.create_dd.content.jei.FanSandingCategory, but 0.2c removed it.)
 * This mixin uses a string target so {@link plus.dragons.createdragonsplus.mixin.CDPMixinConfigPlugin}
 * will safely skip it when the target class does not exist. It will automatically activate
 * if a future DnD version adds this category class.
 * <p>
 * Version note: On Forge 1.20.1, DnDesires uses mod ID {@code create_dd} with package
 * {@code uwu.lopyluna.create_dd}. The rename to {@code dndesires} / {@code dev.lopyluna.dndesires}
 * only applies to NeoForge 1.21.1+. The targets and imports here are correct for Forge 1.20.1.
 */
@Restriction(require = @Condition(Constants.CREATE_DND))
@Mixin(targets = "uwu.lopyluna.create_dd.jei.fan.FanSandingCategory", remap = false)
public abstract class FanSandingCategoryMixinForDnD<T extends ProcessingRecipe<?>> extends DDProcessingViaFanCategory.MultiOutput<T> {
    private FanSandingCategoryMixinForDnD(Info<T> info) {
        super(info);
    }

    @Override
    public boolean isHandled(T recipe) {
        return !CDPConfig.server().enableBulkSanding.get();
    }
}
