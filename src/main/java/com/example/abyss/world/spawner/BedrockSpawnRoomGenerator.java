package com.example.abyss.world.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BedrockSpawnRoomGenerator {

    private static BlockPos cachedChamberCenter = null;

    public static BlockPos getOrCreateSafeBedrockRoom(ServerLevel level) {
        if (cachedChamberCenter != null) {
            return cachedChamberCenter;
        }

        // Scan Layer 1 (Y: 1920 down to 1350) in a spiral to locate the closest natural cliff ledge
        // so that descending through the floor hole lands on solid ground instead of the central void.
        BlockPos surfacePos = findNearestLedge(level, new BlockPos(0, 1850, 0), 512);

        // Place the bedrock bunker 14 blocks directly above the solid cliff surface
        BlockPos roomCenter = surfacePos.above(14);
        buildBedrockChamber(level, roomCenter, surfacePos);

        cachedChamberCenter = roomCenter;
        return roomCenter;
    }

    private static BlockPos findNearestLedge(ServerLevel level, BlockPos origin, int maxRadius) {
        for (int r = 24; r <= maxRadius; r += 16) {
            for (int angle = 0; angle < 360; angle += 15) {
                double rad = Math.toRadians(angle);
                int x = origin.getX() + (int) (r * Math.cos(rad));
                int z = origin.getZ() + (int) (r * Math.sin(rad));

                // Ensure chunk is loaded before scanning column
                level.getChunk(x >> 4, z >> 4);

                for (int y = 1920; y > 1350; y--) {
                    BlockPos check = new BlockPos(x, y, z);
                    BlockState ground = level.getBlockState(check);
                    BlockState above1 = level.getBlockState(check.above());
                    BlockState above2 = level.getBlockState(check.above(2));

                    if (ground.isSolidRender(level, check) && above1.isAir() && above2.isAir()) {
                        if (isStablePlatform(level, check)) {
                            return check;
                        }
                    }
                }
            }
        }

        // Fallback: build a natural basalt/tuff cliff outcrop attached to the crater wall
        BlockPos fallback = new BlockPos(96, 1780, 0);
        createFallbackLedge(level, fallback);
        return fallback;
    }

    private static boolean isStablePlatform(ServerLevel level, BlockPos center) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                if (!level.getBlockState(p).isSolidRender(level, p)) return false;
            }
        }
        return true;
    }

    private static void createFallbackLedge(ServerLevel level, BlockPos center) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (dx * dx + dz * dz <= 38) {
                    for (int dy = -4; dy <= 0; dy++) {
                        level.setBlock(center.offset(dx, dy, dz),
                                dy == 0 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.TUFF.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void buildBedrockChamber(ServerLevel level, BlockPos center, BlockPos groundPos) {
        int radius = 4; // 9x9x6 Bedrock Bunker
        int height = 5;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= height; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    boolean isWall = Math.abs(dx) == radius || Math.abs(dz) == radius || dy == 0 || dy == height;
                    boolean isHole = (dy == 0) && (Math.abs(dx) <= 1 && Math.abs(dz) <= 1); // 3x3 Hatch in floor

                    if (isHole) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (isWall) {
                        level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Atmospheric Soul Lanterns inside the Bedrock Room
        level.setBlock(center.offset(-3, 4, -3), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(center.offset(3, 4, -3), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(center.offset(-3, 4, 3), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(center.offset(3, 4, 3), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // Hang heavy chains and climbable weeping vines from the hole directly to the surface below
        for (int y = center.getY() - 1; y > groundPos.getY(); y--) {
            level.setBlock(new BlockPos(center.getX() - 1, y, center.getZ() - 1), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(center.getX() + 1, y, center.getZ() + 1), Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(center.getX() - 1, y, center.getZ() + 1), Blocks.SCAFFOLDING.defaultBlockState(), 2);
        }

        // Cushioning moss and water pocket on the surface directly beneath the hatch
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(groundPos.offset(dx, 0, dz), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
            }
        }
        level.setBlock(groundPos, Blocks.WATER.defaultBlockState(), 2);
    }

    public static void teleportPlayerToSpawnChamber(ServerPlayer player, ServerLevel abyssLevel) {
        BlockPos roomCenter = getOrCreateSafeBedrockRoom(abyssLevel);
        // Spawn on the bedrock rim inside the chamber (X+2.5), looking down at the 3x3 descent hole
        player.teleportTo(abyssLevel, roomCenter.getX() + 2.5, roomCenter.getY() + 1.0, roomCenter.getZ() + 0.5, 90f, 35f);
        player.sendSystemMessage(Component.literal("§6[Орт — Край Бездны] §7Вы находитесь в распределительном бункере над Первым Слоем. Спуститесь в люк по цепям, чтобы начать погружение."));
    }
}
