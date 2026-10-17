package com.example.abyss.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Layer 4 (Y: 0 to -700) Unique Geo-Structure: Goblets of Giants.
 * Generates colossal concave terraces filled with warm mineral water and Eternal Fortunes flowers.
 */
public class GiantGobletFeature extends Feature<NoneFeatureConfiguration> {

    public GiantGobletFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource rand = ctx.random();

        if (origin.getY() > 50 || origin.getY() < -750) return false;

        int stemHeight = 25 + rand.nextInt(35);
        int cupRadius = 11 + rand.nextInt(5);

        BlockState gobletStone = Blocks.PRISMARINE.defaultBlockState();
        BlockState gobletRim = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState fluid = Blocks.WATER.defaultBlockState();

        // 1. Stem of the Giant Goblet
        for (int dy = 0; dy < stemHeight; dy++) {
            int r = Math.max(3, (int) (cupRadius * 0.35 * (1.0 - Math.sin((double) dy / stemHeight * Math.PI) * 0.4)));
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        level.setBlock(origin.offset(dx, dy, dz), gobletStone, 2);
                    }
                }
            }
        }

        // 2. Concave Cup Basin with fluid and luminescent spores
        BlockPos cupCenter = origin.above(stemHeight);
        for (int dy = 0; dy <= 10; dy++) {
            double r = cupRadius * (0.5 + (dy / 20.0));
            int rCeil = (int) Math.ceil(r);
            for (int dx = -rCeil; dx <= rCeil; dx++) {
                for (int dz = -rCeil; dz <= rCeil; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (distSq <= r * r) {
                        BlockPos p = cupCenter.offset(dx, dy, dz);
                        if (dy < 3 || distSq >= (r - 1.5) * (r - 1.5)) {
                            level.setBlock(p, dy == 10 ? gobletRim : gobletStone, 2);
                            if (dy == 10 && rand.nextFloat() < 0.3f) {
                                // Eternal Fortunes (White/Blue flowers along the rim)
                                level.setBlock(p.above(), Blocks.LILY_OF_THE_VALLEY.defaultBlockState(), 2);
                            }
                        } else if (dy <= 8) {
                            level.setBlock(p, fluid, 2);
                        } else {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        return true;
    }
}
