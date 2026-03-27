/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles.PackageStyle;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import java.util.EnumMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.client.texture.CDPGuiTextures;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;

public class CDPItems {
    // Tags
    public static final TagKey<Item> DYE_BUCKETS = ItemTags.create(new ResourceLocation("c", "buckets/dye"));
    public static final EnumMap<DyeColor, TagKey<Item>> DYE_BUCKETS_BY_COLOR = Util.make(new EnumMap<>(DyeColor.class), map -> {
        for (DyeColor color : DyeColors.ALL) {
            map.put(color, ItemTags.create(new ResourceLocation("c", "buckets/dye/" + color.getName())));
        }
    });
    public static final TagKey<Item> DRAGON_BREATH_BUCKETS = ItemTags.create(new ResourceLocation("c", "buckets/dragon_breath"));
    public static final TagKey<Item> NOT_APPLICABLE_COLORING = ItemTags.create(new ResourceLocation(CDPCommon.ID, "not_applicable_for_coloring"));

    /** @deprecated Use static fields directly. Kept for API compatibility. */
    @Deprecated
    public static class CommonTags {
        public final TagKey<Item> dyeBuckets = DYE_BUCKETS;
        public final EnumMap<DyeColor, TagKey<Item>> dyeBucketsByColor = DYE_BUCKETS_BY_COLOR;
        public final TagKey<Item> dragonBreathBuckets = DRAGON_BREATH_BUCKETS;
    }

    /** @deprecated Use static fields directly. Kept for API compatibility. */
    @Deprecated
    public static class ModTags {
        public final TagKey<Item> notApplicableColoring = NOT_APPLICABLE_COLORING;
    }

    /** @deprecated Use static fields directly. Kept for API compatibility. */
    @Deprecated
    public static final CommonTags COMMON_TAGS = new CommonTags();

    /** @deprecated Use static fields directly. Kept for API compatibility. */
    @Deprecated
    public static final ModTags MOD_TAGS = new ModTags();

    public static final ItemEntry<PackageItem> RARE_BLAZE_PACKAGE = REGISTRATE
            .item("rare_blaze_pacakge", prop -> new PackageItem(prop,
                    new PackageStyle("rare_blaze", 12, 10, 21, true)))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .properties(prop -> prop.stacksTo(1).fireResistant())
            .tag(AllItemTags.PACKAGES.tag)
            .model((ctx, prov) -> prov
                    .withExistingParent(ctx.getName(), Create.asResource("item/package/custom_12x10"))
                    .texture("2", prov.modLoc("item/package/rare_blaze")))
            .register();
    public static final ItemEntry<PackageItem> RARE_MARBLE_GATE_PACKAGE = REGISTRATE
            .item("rare_marble_gate_pacakge", prop -> new PackageItem(prop,
                    new PackageStyle("rare_marble_gate", 12, 10, 21, true)))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .properties(prop -> prop.stacksTo(1))
            .tag(AllItemTags.PACKAGES.tag)
            .model((ctx, prov) -> prov
                    .withExistingParent(ctx.getName(), Create.asResource("item/package/custom_12x10"))
                    .texture("2", prov.modLoc("item/package/rare_marble_gate")))
            .register();
    public static final ItemEntry<SmithingTemplateItem> BLAZE_UPGRADE_SMITHING_TEMPLATE = REGISTRATE
            .item("blaze_upgrade_smithing_template", prop -> new SmithingTemplateItem(
                    Tooltips.BLAZE_UPGRADE_APPLIES_TO,
                    Tooltips.BLAZE_UPGRADE_INGREDIENTS,
                    Tooltips.BLAZE_UPGRADE,
                    Tooltips.BLAZE_UPGRADE_BASE_SLOT,
                    Tooltips.BLAZE_UPGRADE_ADDITIONS_SLOT,
                    CDPGuiTextures.BLAZE_UPGRADE_BASE_SLOT_ICONS,
                    CDPGuiTextures.BLAZE_UPGRADE_ADDITIONS_SLOT_ICONS))
            .lang("Smithing Template")
            .register();

    public static void register(IEventBus modBus) {
        // Force class loading
    }

    public static class Tooltips {
        private static final ResourceLocation BLAZE_UPGRADE_SMITHING_TEMPLATE = CDPCommon.asResource("smithing_template.blaze_upgrade");
        public static final Component BLAZE_UPGRADE_APPLIES_TO = REGISTRATE.addLang("item",
                BLAZE_UPGRADE_SMITHING_TEMPLATE.withSuffix(".applies_to"),
                "Blaze Burner").withStyle(ChatFormatting.BLUE);
        public static final Component BLAZE_UPGRADE_INGREDIENTS = REGISTRATE.addLang("item",
                BLAZE_UPGRADE_SMITHING_TEMPLATE.withSuffix(".ingredients"),
                "Working blocks for Blaze").withStyle(ChatFormatting.BLUE);
        public static final Component BLAZE_UPGRADE = REGISTRATE.addLang("upgrade",
                CDPCommon.asResource("blaze_upgrade"),
                "Blaze Upgrade").withStyle(ChatFormatting.GRAY);
        public static final Component BLAZE_UPGRADE_BASE_SLOT = REGISTRATE.addLang("item",
                BLAZE_UPGRADE_SMITHING_TEMPLATE.withSuffix(".base_slot_description"),
                "Add Blaze Burner");
        public static final Component BLAZE_UPGRADE_ADDITIONS_SLOT = REGISTRATE.addLang("item",
                BLAZE_UPGRADE_SMITHING_TEMPLATE.withSuffix(".additions_slot_description"),
                "Add working blocks for Blaze");
    }
}
