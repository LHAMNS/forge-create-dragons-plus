/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
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

package plus.dragons.createdragonsplus.common.processing.freeze;

import net.createmod.catnip.lang.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import plus.dragons.createdragonsplus.common.CDPCommon;

/**
 * Enumeration of freeze conditions for freeze processing recipes,
 * indicating the required level of freezing (passive, frozen, superfrozen).
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- removes Codec/StreamCodec
 * (replaced with StringRepresentable for JSON serialization), removes CDPLang
 * reference and uses Lang.builder directly.
 */
public enum FreezeCondition implements StringRepresentable {
    PASSIVE(0xFFFFFF),
    FROZEN(0x8ADCE8),
    SUPERFROZEN(0x5C93E8);

    private final int color;

    FreezeCondition(int color) {
        this.color = color;
    }

    public boolean testFreezer(float freeze) {
        return switch (this) {
            case PASSIVE -> freeze >= 0;
            case FROZEN -> freeze >= 1;
            case SUPERFROZEN -> freeze >= 2;
        };
    }

    public Component getComponent() {
        return Lang.builder(CDPCommon.ID)
                .translate("recipe.freeze_condition")
                .component();
    }

    public int getColor() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return Lang.asId(name());
    }

    /**
     * Parse a FreezeCondition from its serialized name.
     */
    public static FreezeCondition fromString(String name) {
        for (FreezeCondition condition : values()) {
            if (condition.getSerializedName().equals(name)) {
                return condition;
            }
        }
        return PASSIVE;
    }
}
