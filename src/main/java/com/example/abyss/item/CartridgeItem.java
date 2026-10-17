package com.example.abyss.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bondrewd's Cartridge: When held in offhand or hotbar, intercepts and nullifies
 * the Curse of the Abyss (especially on Layers 4, 5, and 6), taking durability damage instead.
 */
public class CartridgeItem extends Item {
    public CartridgeItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5Картридж Передовой (Идофронт)"));
        tooltip.add(Component.literal("§7Поглощает удар Проклятия Бездны при подъеме на 4-6 слоях."));
        tooltip.add(Component.literal("§cОсталось зарядов фильтрации: " + (stack.getMaxDamage() - stack.getDamageValue())));
    }
}
