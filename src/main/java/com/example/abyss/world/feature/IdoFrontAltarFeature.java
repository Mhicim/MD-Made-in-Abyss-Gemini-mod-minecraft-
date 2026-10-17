package com.example.abyss.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Layer 5 (Y: -1350 to -1400) Unique Lore Structure: Ido Front Ritual Altar & 6th Layer Dive Pod.
 */
public class IdoFrontAltarFeature extends Feature<NoneFeatureConfiguration> {

    public IdoFrontAltarFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();

        if (origin.getY() > -1200 || origin.getY() < -1450) return false;

        BlockState obsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState deepslate = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState seaLantern = Blocks.SEA_LANTERN.defaultBlockState();

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq <= 64) {
                    level.setBlock(origin.offset(dx, 0, dz), deepslate, 2);
                    if (distSq >= 49 && (dx % 3 == 0 || dz % 3 == 0)) {
                        for (int dy = 1; dy <= 6; dy++) {
                            level.setBlock(origin.offset(dx, dy, dz), obsidian, 2);
                        }
                        level.setBlock(origin.offset(dx, 7, dz), seaLantern, 2);
                    }
                }
            }
        }

        // Central Altar of the Absolute Boundary (Respawn Anchor + Lodestone)
        level.setBlock(origin.above(1), Blocks.LODESTONE.defaultBlockState(), 2);
        level.setBlock(origin.above(2), Blocks.RESPAWN_ANCHOR.defaultBlockState(), 2);

        return true;
    }
}
