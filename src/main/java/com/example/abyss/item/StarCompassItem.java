package com.example.abyss.item;

import com.example.abyss.world.AbyssLayerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;

/**
 * Star Compass Relic: Displays the player's current vertical coordinate, Layer name,
 * and inspects the cross-mod biomes & mobs dynamically assigned to the current layer.
 */
public class StarCompassItem extends Item {
    public StarCompassItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int y = (int) player.getY();
            var boundary = AbyssLayerManager.getLayerBoundaryForY(y);
            var pool = AbyssLayerManager.getPool(boundary.id());

            String sampleBiomes = pool.biomes.stream().limit(4).map(ResourceLocation::toString).collect(Collectors.joining(", "));
            player.sendSystemMessage(Component.literal("§6=== [Звездный Компас Бездны] ==="));
            player.sendSystemMessage(Component.literal("§eТекущая глубина: §fY = " + y + " §7| Слой " + boundary.id() + ": §b" + boundary.name()));
            player.sendSystemMessage(Component.literal("§aПул биомов слоя (" + pool.biomes.size() + "): §7" + sampleBiomes + "..."));
            player.sendSystemMessage(Component.literal("§cПул мобов слоя: §f" + pool.mobs.size() + " видов §7(Множитель силы: x" + boundary.mobStatMultiplier() + ")"));
            player.sendSystemMessage(Component.literal("§dПул структур слоя: §f" + pool.structures.size() + " типов"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
