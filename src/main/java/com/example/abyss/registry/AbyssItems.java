package com.example.abyss.registry;

import com.example.abyss.AbyssCoreMod;
import com.example.abyss.item.CartridgeItem;
import com.example.abyss.item.IncineratorRelicItem;
import com.example.abyss.item.StarCompassItem;
import com.example.abyss.item.WhistleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AbyssItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AbyssCoreMod.MODID);

    // Delver Whistles (Red -> Blue -> Moon -> Black -> White)
    public static final RegistryObject<Item> RED_WHISTLE = ITEMS.register("red_whistle",
            () -> new WhistleItem(1, "Красный свисток (Новичок)", new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> BLUE_WHISTLE = ITEMS.register("blue_whistle",
            () -> new WhistleItem(2, "Синий свисток (Адепт)", new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MOON_WHISTLE = ITEMS.register("moon_whistle",
            () -> new WhistleItem(3, "Лунный свисток (Наставник)", new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BLACK_WHISTLE = ITEMS.register("black_whistle",
            () -> new WhistleItem(4, "Черный свисток (Эксперт)", new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WHITE_WHISTLE = ITEMS.register("white_whistle",
            () -> new WhistleItem(5, "Белый свисток (Суверен Бездны)", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // Bondrewd's Curse-Absorbing Cartridge
    public static final RegistryObject<Item> BONDRWD_CARTRIDGE = ITEMS.register("bondrewd_cartridge",
            () -> new CartridgeItem(new Item.Properties().durability(12).rarity(Rarity.RARE)));

    // Special Grade Relics
    public static final RegistryObject<Item> INCINERATOR_RELIC = ITEMS.register("incinerator_relic",
            () -> new IncineratorRelicItem(new Item.Properties().durability(32).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> STAR_COMPASS = ITEMS.register("star_compass",
            () -> new StarCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
}
