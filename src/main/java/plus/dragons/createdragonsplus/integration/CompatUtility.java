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

package plus.dragons.createdragonsplus.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class CompatUtility {
    private static Item industrialFan;
    private static boolean industrialFanChecked = false;

    public static List<Supplier<? extends ItemStack>> catalystWithIndustryFan(ItemStack fan) {
        if (!industrialFanChecked) {
            industrialFanChecked = true;
            industrialFan = ForgeRegistries.ITEMS.getValue(ModIntegration.CREATE_DND.asResource("industrial_fan"));
        }
        List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();
        catalysts.add(() -> fan);
        if (industrialFan != null) {
            catalysts.add(() -> new ItemStack(industrialFan));
        }
        return catalysts;
    }
}
