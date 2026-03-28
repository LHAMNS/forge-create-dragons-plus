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

package plus.dragons.createdragonsplus.common.advancements.criterion;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.core.registries.BuiltInRegistries;
import plus.dragons.createdragonsplus.common.CDPCommon;

public class StatTrigger implements CriterionTrigger<StatTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(CDPCommon.ID, "stat");
    private final Table<PlayerAdvancements, Stat<?>, Set<Listener<Instance>>> listeners = Tables
            .newCustomTable(new IdentityHashMap<>(), IdentityHashMap::new);

    public StatTrigger() {
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public final void addPlayerListener(PlayerAdvancements advancements, CriterionTrigger.Listener<Instance> listener) {
        var stat = listener.getTriggerInstance().stat;
        var set = this.listeners.get(advancements, stat);
        if (set == null) {
            set = new HashSet<>();
            this.listeners.put(advancements, stat, set);
        }
        set.add(listener);
    }

    @Override
    public final void removePlayerListener(PlayerAdvancements advancements, CriterionTrigger.Listener<Instance> listener) {
        var stat = listener.getTriggerInstance().stat;
        var set = this.listeners.get(advancements, stat);
        if (set != null) {
            set.remove(listener);
            if (set.isEmpty())
                this.listeners.remove(advancements, stat);
        }
    }

    @Override
    public final void removePlayerListeners(PlayerAdvancements advancements) {
        this.listeners.rowMap().remove(advancements);
    }

    @Override
    public Instance createInstance(JsonObject json, DeserializationContext context) {
        ResourceLocation statTypeId = new ResourceLocation(json.get("stat_type").getAsString());
        ResourceLocation statId = new ResourceLocation(json.get("stat").getAsString());
        MinMaxBounds.Ints bounds = MinMaxBounds.Ints.fromJson(json.get("bounds"));
        @SuppressWarnings("unchecked")
        StatType<Object> statType = (StatType<Object>) BuiltInRegistries.STAT_TYPE.get(statTypeId);
        if (statType == null)
            throw new IllegalArgumentException("Unknown stat type: " + statTypeId);
        Object statValue = statType.getRegistry().get(statId);
        if (statValue == null)
            throw new IllegalArgumentException("Unknown stat: " + statId);
        Stat<?> stat = statType.get(statValue);
        return new Instance(stat, bounds);
    }

    public void trigger(ServerPlayer player, Stat<?> stat, int value) {
        PlayerAdvancements advancements = player.getAdvancements();
        var listeners = this.listeners.get(advancements, stat);
        if (listeners == null || listeners.isEmpty())
            return;
        // Collect matching listeners before running to avoid ConcurrentModificationException,
        // since listener.run() may modify the listener set via addPlayerListener/removePlayerListener
        List<Listener<Instance>> toRun = null;
        for (var listener : listeners) {
            var trigger = listener.getTriggerInstance();
            if (trigger.bounds.matches(value)) {
                if (toRun == null) toRun = new ArrayList<>();
                toRun.add(listener);
            }
        }
        if (toRun != null) {
            for (var listener : toRun) {
                listener.run(advancements);
            }
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        final Stat<?> stat;
        final MinMaxBounds.Ints bounds;

        public Instance(Stat<?> stat, MinMaxBounds.Ints bounds) {
            super(ID, null);
            this.stat = stat;
            this.bounds = bounds;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            @SuppressWarnings("unchecked")
            StatType<Object> statType = (StatType<Object>) stat.getType();
            json.addProperty("stat_type", BuiltInRegistries.STAT_TYPE.getKey(statType).toString());
            json.addProperty("stat", statType.getRegistry().getKey(stat.getValue()).toString());
            json.add("bounds", bounds.serializeToJson());
            return json;
        }
    }
}
