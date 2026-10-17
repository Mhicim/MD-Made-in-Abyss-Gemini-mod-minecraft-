package com.example.abyss.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public class AbyssCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue BIOMES_PER_LAYER;
    public static final ForgeConfigSpec.IntValue MOBS_PER_LAYER;
    public static final ForgeConfigSpec.IntValue STRUCTURES_PER_LAYER;
    public static final ForgeConfigSpec.DoubleValue CURSE_ASCENT_THRESHOLD;
    public static final ForgeConfigSpec.IntValue MIN_COLOSSAL_TREE_HEIGHT;
    public static final ForgeConfigSpec.IntValue MAX_COLOSSAL_TREE_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_MODS;

    static {
        BUILDER.push("Modular Cross-Mod Layer Settings");
        BIOMES_PER_LAYER = BUILDER.comment("Number of random biomes assigned to each of the 7 Abyss layers")
                .defineInRange("biomesPerLayer", 10, 1, 50);
        MOBS_PER_LAYER = BUILDER.comment("Number of random mobs assigned to each of the 7 Abyss layers, sorted by threat power")
                .defineInRange("mobsPerLayer", 20, 1, 100);
        STRUCTURES_PER_LAYER = BUILDER.comment("Number of random structures assigned to each of the 7 Abyss layers")
                .defineInRange("structuresPerLayer", 10, 1, 50);
        BLACKLISTED_MODS = BUILDER.comment("Mod namespaces to exclude from dynamic layer pooling")
                .defineList("blacklistedMods", List.of("jei", "journeymap"), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.push("Worldgen & Curse Settings");
        CURSE_ASCENT_THRESHOLD = BUILDER.comment("Vertical blocks ascended before triggering the Curse of the Abyss")
                .defineInRange("curseAscentThreshold", 10.0, 2.0, 64.0);
        MIN_COLOSSAL_TREE_HEIGHT = BUILDER.comment("Minimum height of colossal Abyss trees")
                .defineInRange("minColossalTreeHeight", 200, 100, 500);
        MAX_COLOSSAL_TREE_HEIGHT = BUILDER.comment("Maximum height of colossal Abyss trees")
                .defineInRange("maxColossalTreeHeight", 1000, 500, 1500);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
