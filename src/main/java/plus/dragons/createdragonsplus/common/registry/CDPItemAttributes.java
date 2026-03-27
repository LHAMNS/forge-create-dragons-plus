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

package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createdragonsplus.common.CDPCommon;

public class CDPItemAttributes {
    private static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTES = DeferredRegister
            .create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, CDPCommon.ID);

    public static final RegistryObject<ItemAttributeType> FREEZABLE = fanProcessing("freezable",
            "can be Frozen",
            "cannot be Frozen",
            () -> CDPFanProcessingTypes.FREEZING);
    public static final RegistryObject<ItemAttributeType> SANDABLE = fanProcessing("sandable",
            "can be Sanded",
            "cannot be Sanded",
            () -> CDPFanProcessingTypes.SANDING);
    public static final RegistryObject<ItemAttributeType> ENDABLE = fanProcessing("endable",
            "can be Ended",
            "cannot be Ended",
            () -> CDPFanProcessingTypes.ENDING);
    public static final RegistryObject<ItemAttributeType> STAINABLE = colorFanProcessing();

    private static RegistryObject<ItemAttributeType> fanProcessing(String name, String description, String invertedDescription, Supplier<? extends FanProcessingType> processingType) {
        String descriptionKey = "create.item_attributes." + CDPCommon.ID + "." + name;
        String invertedDescriptionKey = descriptionKey + ".inverted";
        REGISTRATE.addRawLang(descriptionKey, description);
        REGISTRATE.addRawLang(invertedDescriptionKey, invertedDescription);
        return ITEM_ATTRIBUTES.register(name, () -> new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, processingType.get()::canProcess, CDPCommon.ID + "." + name)));
    }

    private static RegistryObject<ItemAttributeType> colorFanProcessing() {
        String descriptionKey = "create.item_attributes." + CDPCommon.ID + ".stainable";
        String invertedDescriptionKey = descriptionKey + ".inverted";
        REGISTRATE.addRawLang(descriptionKey, "can be Stained");
        REGISTRATE.addRawLang(invertedDescriptionKey, "cannot be Stained");
        return ITEM_ATTRIBUTES.register("stainable", () -> new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type,
                (itemStack, level) -> CDPFanProcessingTypes.COLORING.values().stream()
                        .anyMatch(colorType -> colorType.canProcess(itemStack, level)),
                CDPCommon.ID + ".stainable")));
    }

    public static void register(IEventBus modBus) {
        ITEM_ATTRIBUTES.register(modBus);
    }
}
