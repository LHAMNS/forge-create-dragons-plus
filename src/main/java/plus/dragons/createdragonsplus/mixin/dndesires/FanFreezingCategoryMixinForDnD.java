/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
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

package plus.dragons.createdragonsplus.mixin.dndesires;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.ModIntegration.Constants;
import uwu.lopyluna.create_dd.jei.fan.DDProcessingViaFanCategory;
import uwu.lopyluna.create_dd.jei.fan.FanFreezingCategory;

@Restriction(require = @Condition(Constants.CREATE_DND))
@Mixin(value = FanFreezingCategory.class, remap = false)
public abstract class FanFreezingCategoryMixinForDnD<T extends ProcessingRecipe<?>> extends DDProcessingViaFanCategory.MultiOutput<T> {
    private FanFreezingCategoryMixinForDnD(Info<T> info) {
        super(info);
    }

    @Override
    public boolean isHandled(T recipe) {
        return !CDPConfig.server().enableBulkFreezing.get();
    }
}
