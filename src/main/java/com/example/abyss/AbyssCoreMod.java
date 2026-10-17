package com.example.abyss;

import com.example.abyss.config.AbyssCommonConfig;
import com.example.abyss.registry.AbyssFeatures;
import com.example.abyss.registry.AbyssItems;
import com.example.abyss.world.AbyssLayerManager;
import com.example.abyss.world.spawner.BedrockSpawnRoomGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(AbyssCoreMod.MODID)
public class AbyssCoreMod {
    public static final String MODID = "abyss_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Level> ABYSS_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "the_abyss")
    );

    public AbyssCoreMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AbyssItems.ITEMS.register(modEventBus);
        AbyssFeatures.FEATURES.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AbyssCommonConfig.SPEC, "abyss_core-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        LOGGER.info("[AbyssCore] Initializing cross-mod dynamic layer pools (10 Biomes, 20 Mobs, 10 Structures per Layer)...");
        AbyssLayerManager.initializePools(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.getPersistentData().getBoolean("abyss_spawn_initialized")) {
                ServerLevel abyssLevel = player.server.getLevel(ABYSS_LEVEL_KEY);
                if (abyssLevel == null) {
                    abyssLevel = player.serverLevel();
                }
                BedrockSpawnRoomGenerator.teleportPlayerToSpawnChamber(player, abyssLevel);
                player.getPersistentData().putBoolean("abyss_spawn_initialized", true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isEndConquered()) {
            if (player.getRespawnPosition() == null) {
                ServerLevel abyssLevel = player.server.getLevel(ABYSS_LEVEL_KEY);
                if (abyssLevel == null) {
                    abyssLevel = player.serverLevel();
                }
                BedrockSpawnRoomGenerator.teleportPlayerToSpawnChamber(player, abyssLevel);
            }
        }
    }
}
