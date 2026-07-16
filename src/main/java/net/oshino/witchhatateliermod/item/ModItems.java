package net.oshino.witchhatateliermod.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.oshino.witchhatateliermod.WitchHatAtelierMod;

public final class ModItems {
    public static final Item SPELLBOOK = Registry.register(
            Registries.ITEM,
            WitchHatAtelierMod.id("spellbook"),
            new Item(new Item.Settings().maxCount(1))
    );

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(SPELLBOOK));
    }
}
