package com.example.abyss.item;

import com.example.abyss.world.AbyssLayerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WhistleItem extends Item {
    private final int rank;
    private final String title;

    public WhistleItem(int rank, String title, Properties props) {
        super(props);
        this.rank = rank;
        this.title = title;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int currentLayer = AbyssLayerManager.getLayerForY((int) player.getY());
            level.playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(),
                    SoundSource.PLAYERS, 1.5f, 0.8f + (rank * 0.15f));

            // Resonating a Whistle empowers Relics and grants layer resistance
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200 * rank, Math.min(2, rank - 1)));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400 * rank, 0));

            player.sendSystemMessage(Component.literal("§b[" + title + "] §7Резонанс активирован на Слое " + currentLayer + "."));
            player.getCooldowns().addCooldown(this, 300);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
