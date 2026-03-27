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

package plus.dragons.createdragonsplus.mixin;

import java.util.List;
import java.util.Set;
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.service.MixinService;

/**
 * Mixin config plugin for Create Dragons Plus.
 * <p>
 * Extends RestrictiveMixinConfigPlugin from conditional-mixin to support
 * {@link me.fallenbreath.conditionalmixin.api.annotation.Restriction} annotations
 * for conditional mixin loading based on mod presence.
 * <p>
 * Additionally checks that the mixin's target class actually exists on the classpath,
 * so mixins using string-based {@code targets} for classes that may not be present
 * (e.g., future DnD categories) are safely skipped without crashing.
 */
public class CDPMixinConfigPlugin extends RestrictiveMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // First check the @Restriction annotation (mod presence check)
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) {
            return false;
        }
        // Then verify the target class actually exists on the classpath.
        // This prevents crashes when a mixin uses string-based targets for classes
        // that don't exist in the current version of a dependency (e.g., DnD 0.2c
        // lacks FanDragonBreathingCategory and FanSandingCategory).
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(targetClassName.replace('.', '/'));
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            // Catch any other exception (e.g., IOException) to be safe
            return false;
        }
    }

    @Override
    @Nullable
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    @Nullable
    public List<String> getMixins() {
        return null;
    }
}
