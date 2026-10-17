package com.example.abyss.world.feature;

import com.example.abyss.config.AbyssCommonConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ColossalAbyssTreeFeature extends Feature<NoneFeatureConfiguration> {

    public ColossalAbyssTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource rand = ctx.random();

        int minH = AbyssCommonConfig.MIN_COLOSSAL_TREE_HEIGHT.get();
        int maxH = AbyssCommonConfig.MAX_COLOSSAL_TREE_HEIGHT.get();
        int treeHeight = minH + rand.nextInt(Math.max(1, maxH - minH + 1));

        if (origin.getY() + treeHeight > 2015) {
            treeHeight = Math.max(200, 2015 - origin.getY());
        }

        int baseRadius = Mth.clamp(treeHeight / 45, 5, 15);
        BlockState bark = Blocks.DARK_OAK_WOOD.defaultBlockState();
        BlockState heartwood = Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
        BlockState leaves = Blocks.AZALEA_LEAVES.defaultBlockState();
        BlockState glow = Blocks.SHROOMLIGHT.defaultBlockState();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dy = -35; dy <= treeHeight; dy++) {
            float progress = Math.max(0f, (float) dy / treeHeight);
            double currentRadius = dy < 0
                    ? baseRadius * (1.0 - (dy / 45.0))
                    : baseRadius * (1.0 - progress * 0.65);

            int swayX = (int) (Math.sin(dy * 0.015) * (baseRadius * 0.75));
            int swayZ = (int) (Math.cos(dy * 0.015) * (baseRadius * 0.75));

            int rCeil = (int) Math.ceil(currentRadius);
            for (int dx = -rCeil; dx <= rCeil; dx++) {
                for (int dz = -rCeil; dz <= rCeil; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (distSq <= currentRadius * currentRadius) {
                        cursor.set(origin.getX() + swayX + dx, origin.getY() + dy, origin.getZ() + swayZ + dz);

                        // Hollow interior with spiral ledges and shroomlights
                        if (dy > 12 && dy < treeHeight - 30 && distSq < (currentRadius - 2.2) * (currentRadius - 2.2)) {
                            double angle = Math.atan2(dz, dx);
                            double spiralTarget = ((dy * 0.15) % (Math.PI * 2)) - Math.PI;
                            if (Math.abs(angle - spiralTarget) < 0.35) {
                                level.setBlock(cursor, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                            } else if (dy % 40 == 0 && rand.nextFloat() < 0.2f) {
                                level.setBlock(cursor, glow, 2);
                            } else {
                                level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                            }
                        } else if (distSq >= (currentRadius - 1.2) * (currentRadius - 1.2)) {
                            level.setBlock(cursor, bark, 2);
                        } else {
                            level.setBlock(cursor, heartwood, 2);
                        }
                    }
                }
            }

            if (dy > 50 && dy % 36 == 0) {
                generateColossalBranch(level, origin.offset(swayX, dy, swayZ), rand, bark, leaves, glow);
            }
        }
        return true;
    }

    private void generateColossalBranch(WorldGenLevel level, BlockPos start, RandomSource rand,
                                        BlockState bark, BlockState leaves, BlockState glow) {
        double angle = rand.nextDouble() * Math.PI * 2;
        int length = 10 + rand.nextInt(12);

        for (int i = 1; i <= length; i++) {
            int bx = start.getX() + (int) (Math.cos(angle) * i);
            int bz = start.getZ() + (int) (Math.sin(angle) * i);
            int by = start.getY() + (i / 4);

            BlockPos bPos = new BlockPos(bx, by, bz);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlock(bPos.offset(dx, 0, dz), bark, 2);
                }
            }

            if (i == length) {
                for (int lx = -4; lx <= 4; lx++) {
                    for (int lz = -4; lz <= 4; lz++) {
                        for (int ly = -2; ly <= 2; ly++) {
                            if (lx * lx + ly * ly * 2 + lz * lz <= 18) {
                                level.setBlock(bPos.offset(lx, ly, lz), leaves, 2);
                            }
                        }
                        if (rand.nextFloat() < 0.35f) {
                            int vineLen = 6 + rand.nextInt(16);
                            for (int v = 1; v <= vineLen; v++) {
                                level.setBlock(bPos.offset(lx, -2 - v, lz),
                                        v == vineLen ? glow : Blocks.CAVE_VINES_PLANT.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }
}
