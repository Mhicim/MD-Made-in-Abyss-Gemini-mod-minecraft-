package com.example.abyss.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Aubade Relic: Reg's Incinerator (Канон Света, Переписывающий Правила).
 * Fires a high-energy beam up to 48 blocks, vaporizing blocks and dealing massive true damage.
 */
public class IncineratorRelicItem extends Item {
    public IncineratorRelicItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.5f, 0.7f);

            for (int step = 2; step <= 48; step++) {
                Vec3 point = eyePos.add(look.scale(step));
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 8, 0.4, 0.4, 0.4, 0.05);

                BlockPos bPos = BlockPos.containing(point);
                if (serverLevel.getBlockState(bPos).getDestroySpeed(serverLevel, bPos) >= 0) {
                    serverLevel.destroyBlock(bPos, false);
                }

                List<Entity> targets = serverLevel.getEntities(player, new AABB(bPos).inflate(2.5));
                for (Entity target : targets) {
                    if (target instanceof LivingEntity living) {
                        living.hurt(player.damageSources().sonicBoom(player), 65.0f);
                        living.setSecondsOnFire(10);
                    }
                }
            }

            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            player.getCooldowns().addCooldown(this, 240);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
