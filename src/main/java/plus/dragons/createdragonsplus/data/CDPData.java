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

package plus.dragons.createdragonsplus.data;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.data.internal.CDPRecipeProvider;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Key changes:
 * - No DatagenModLoader.isRunningDataGen() check (handled differently in Forge)
 * - GatherDataEvent from Forge instead of NeoForge
 * - No RegistrateDataMapProvider (NeoForge-only data maps)
 * - CDPRecipeProvider constructor simplified (no CompletableFuture<Provider>)
 * - Uses @Mod.EventBusSubscriber for mod bus events
 */
@Mod.EventBusSubscriber(modid = CDPCommon.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CDPData {

    public static void register(IEventBus modBus) {
        // Registration of datagen providers is handled through GatherDataEvent
        // Localization and ponder registration are handled by CDPRegistrate if used,
        // but CDPCommon.REGISTRATE is CreateRegistrate which does not support these.
    }

    @SubscribeEvent
    public static void generate(final GatherDataEvent event) {
        var server = event.includeServer();
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        generator.addProvider(server, new CDPRecipeProvider(output));
    }
}
