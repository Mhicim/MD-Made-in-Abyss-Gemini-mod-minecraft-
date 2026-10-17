package com.example.abyss.registry;

import com.example.abyss.AbyssCoreMod;
import com.example.abyss.world.feature.ColossalAbyssTreeFeature;
import com.example.abyss.world.feature.GiantGobletFeature;
import com.example.abyss.world.feature.IdoFrontAltarFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AbyssFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, AbyssCoreMod.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> COLOSSAL_ABYSS_TREE =
            FEATURES.register("colossal_abyss_tree", () -> new ColossalAbyssTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GIANT_GOBLET =
            FEATURES.register("giant_goblet", () -> new GiantGobletFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> IDO_FRONT_ALTAR =
            FEATURES.register("ido_front_altar", () -> new IdoFrontAltarFeature(NoneFeatureConfiguration.CODEC));
}
