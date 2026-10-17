package com.example.abyss.curse;

import com.example.abyss.AbyssCoreMod;
import com.example.abyss.config.AbyssCommonConfig;
import com.example.abyss.item.CartridgeItem;
import com.example.abyss.world.AbyssLayerManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AbyssCoreMod.MODID)
public class CurseOfTheAbyssHandler {

    private static final Map<UUID, Double> LOWEST_Y_TRACKER = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        if (!(event.player instanceof ServerPlayer player) || player.isCreative() || player.isSpectator()) return;

        UUID id = player.getUUID();
        double currentY = player.getY();
        double lowestY = LOWEST_Y_TRACKER.getOrDefault(id, currentY);

        // Ambient Forcefield Updraft & Hallucination Events
        handleAmbientAbyssEvents(player, (int) currentY);

        if (currentY < lowestY) {
            LOWEST_Y_TRACKER.put(id, currentY);
            return;
        }

        double threshold = AbyssCommonConfig.CURSE_ASCENT_THRESHOLD.get();
        if (currentY - lowestY >= threshold) {
            int layer = AbyssLayerManager.getLayerForY((int) currentY);

            // Check if player holds a Bondrewd Cartridge to intercept Layer 4-7 Curse
            if (layer >= 4 && tryConsumeCartridge(player, layer)) {
                LOWEST_Y_TRACKER.put(id, currentY);
                return;
            }

            applyLayerCurse(player, layer);
            LOWEST_Y_TRACKER.put(id, currentY);
        }
    }

    private static boolean tryConsumeCartridge(ServerPlayer player, int layer) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof CartridgeItem) {
                stack.hurtAndBreak(1, player, p -> {});
                player.sendSystemMessage(Component.literal("§d[Картридж] §7Фильтр принял удар Проклятия " + layer + "-го слоя на себя! Побочный эффект: Благословение."));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                return true;
            }
        }
        return false;
    }

    private static void handleAmbientAbyssEvents(ServerPlayer player, int y) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();

        // Event 1: Forcefield Updraft Spores every 40 ticks
        if (gameTime % 40 == 0) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    12, 6.0, 4.0, 6.0, 0.02);
        }

        // Event 2: Auditory Hallucinations ("Call of the Abyss") on Layer 3 & 5
        if (gameTime % 1200 == 0 && (y < 600 && y > -1400) && level.random.nextFloat() < 0.25f) {
            level.playSound(null, player.blockPosition().offset(level.random.nextInt(10) - 5, 0, level.random.nextInt(10) - 5),
                    SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.AMBIENT, 0.7f, 0.5f);
            player.sendSystemMessage(Component.literal("§8§oВы слышите чей-то знакомый голос из глубины шахты..."));
        }
    }

    private static void applyLayerCurse(ServerPlayer player, int layer) {
        switch (layer) {
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 1));
            }
            case 3 -> {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
            }
            case 4 -> {
                player.hurt(player.damageSources().magic(), 8.0F);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
            }
            case 5 -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 3));
                player.hurt(player.damageSources().magic(), 12.0F);
            }
            case 6, 7 -> {
                player.sendSystemMessage(Component.literal("§4§l[Бездна] Ваше тело искажается под давлением Силового Поля 6-го слоя (Трансформация в Нарэхатэ)..."));
                player.hurt(player.damageSources().fellOutOfWorld(), 18.0F);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 600, 3));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 4));
            }
        }
    }
}
