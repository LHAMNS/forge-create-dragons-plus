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

package plus.dragons.createdragonsplus.mixin.minecraft;

import javax.annotation.Nullable;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.util.RegistryAliases;

/**
 * Mixin to implement registry alias support on Forge 1.20.1.
 * <p>
 * NeoForge provides {@code Registry.addAlias()} for remapping old IDs to new ones when
 * loading world data (e.g., fluids stored in pipes/tanks with a deprecated ID). Forge 1.20.1
 * lacks this API, so we intercept {@link MappedRegistry#get(ResourceLocation)} to check
 * the alias table when a lookup returns null.
 * <p>
 * Alias resolution is one-level only: if the aliased ID also doesn't exist, null is returned.
 * This prevents infinite recursion even if aliases are misconfigured.
 */
@Mixin(MappedRegistry.class)
public abstract class RegistryAliasMixin<T> {

    /**
     * After {@code get(ResourceLocation)} returns, if the result is null, check the alias
     * table and retry with the remapped key.
     * <p>
     * The re-entrant call to {@code get()} will hit this same mixin again, but since the
     * alias table does not contain the new key as an old key, the second lookup will return
     * whatever the registry has (or null), and no further alias resolution will occur.
     */
    @SuppressWarnings("unchecked")
    @Inject(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void createdragonsplus$resolveAlias(@Nullable ResourceLocation key, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null) return;
        if (key == null) return;
        ResourceLocation alias = RegistryAliases.resolve(key);
        if (alias != null) {
            // One-level resolution: call get() with the new key.
            // This re-enters the method, but the alias map won't contain the new key,
            // so the second invocation will not recurse further.
            T resolved = ((MappedRegistry<T>) (Object) this).get(alias);
            if (resolved != null) {
                cir.setReturnValue(resolved);
            }
        }
    }
}
