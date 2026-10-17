package com.example.abyss.world;

import com.example.abyss.AbyssCoreMod;
import com.example.abyss.config.AbyssCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = AbyssCoreMod.MODID)
public class AbyssLayerManager {

    public record LayerBoundary(int id, String name, int minY, int maxY, float mobStatMultiplier) {}

    public static final List<LayerBoundary> LAYERS = List.of(
        new LayerBoundary(1, "Edge of the Abyss", 1300, 2032, 1.0f),
        new LayerBoundary(2, "Forest of Temptation", 600, 1300, 1.25f),
        new LayerBoundary(3, "Great Fault", 0, 600, 1.6f),
        new LayerBoundary(4, "Goblets of Giants", -700, 0, 2.1f),
        new LayerBoundary(5, "Sea of Corpses", -1400, -700, 2.8f),
        new LayerBoundary(6, "Capital of the Unreturned", -1850, -1400, 3.8f),
        new LayerBoundary(7, "Final Maelstrom", -2032, -1850, 5.0f)
    );

    public static class LayerContent {
        public final List<ResourceLocation> biomes = new ArrayList<>();
        public final List<EntityType<?>> mobs = new ArrayList<>();
        public final List<ResourceLocation> structures = new ArrayList<>();
    }

    private static final Map<Integer, LayerContent> LAYER_POOLS = new HashMap<>();

    public static void initializePools(MinecraftServer server) {
        LAYER_POOLS.clear();
        long seed = server.getWorldData().worldGenOptions().seed();
        Random rand = new Random(seed ^ 0xAB155C0DEL);

        List<? extends String> blacklist = AbyssCommonConfig.BLACKLISTED_MODS.get();

        Registry<Biome> biomeReg = server.registryAccess().registryOrThrow(Registries.BIOME);
        Registry<Structure> structReg = server.registryAccess().registryOrThrow(Registries.STRUCTURE);

        List<ResourceLocation> allBiomes = new ArrayList<>(biomeReg.keySet().stream()
                .filter(rl -> !blacklist.contains(rl.getNamespace()))
                .toList());

        List<ResourceLocation> allStructures = new ArrayList<>(structReg.keySet().stream()
                .filter(rl -> !blacklist.contains(rl.getNamespace()))
                .toList());

        List<EntityType<?>> allMobs = new ArrayList<>(ForgeRegistries.ENTITY_TYPES.getValues().stream()
                .filter(type -> type.getCategory() != MobCategory.MISC)
                .filter(type -> {
                    ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
                    return key != null && !blacklist.contains(key.getNamespace());
                })
                .toList());

        Collections.shuffle(allBiomes, rand);
        Collections.shuffle(allStructures, rand);
        Collections.shuffle(allMobs, rand);

        // Sort partially by threat score + random jitter so deeper layers get deadlier cross-mod creatures
        allMobs.sort(Comparator.comparingDouble(type -> estimateThreatScore(type) + (rand.nextDouble() * 15.0)));

        int biomesPerLayer = AbyssCommonConfig.BIOMES_PER_LAYER.get();
        int mobsPerLayer = AbyssCommonConfig.MOBS_PER_LAYER.get();
        int structsPerLayer = AbyssCommonConfig.STRUCTURES_PER_LAYER.get();

        for (int i = 0; i < LAYERS.size(); i++) {
            int layerId = LAYERS.get(i).id();
            LayerContent content = new LayerContent();

            for (int b = 0; b < biomesPerLayer && !allBiomes.isEmpty(); b++) {
                content.biomes.add(allBiomes.get((i * biomesPerLayer + b) % allBiomes.size()));
            }

            for (int m = 0; m < mobsPerLayer && !allMobs.isEmpty(); m++) {
                int index = (int) (((double) (i * mobsPerLayer + m) / (LAYERS.size() * mobsPerLayer)) * allMobs.size());
                index = Math.min(allMobs.size() - 1, Math.max(0, index));
                content.mobs.add(allMobs.get(index));
            }

            for (int s = 0; s < structsPerLayer && !allStructures.isEmpty(); s++) {
                content.structures.add(allStructures.get((i * structsPerLayer + s) % allStructures.size()));
            }

            LAYER_POOLS.put(layerId, content);
            AbyssCoreMod.LOGGER.info("[AbyssCore] Layer {} ({}) initialized with {} biomes, {} mobs, {} structures.",
                    layerId, LAYERS.get(i).name(), content.biomes.size(), content.mobs.size(), content.structures.size());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static double estimateThreatScore(EntityType<?> type) {
        try {
            if (DefaultAttributes.hasSupplier((EntityType) type)) {
                AttributeSupplier supplier = DefaultAttributes.getSupplier((EntityType) type);
                double hp = supplier.hasAttribute(Attributes.MAX_HEALTH) ? supplier.getValue(Attributes.MAX_HEALTH) : 20.0;
                double dmg = supplier.hasAttribute(Attributes.ATTACK_DAMAGE) ? supplier.getValue(Attributes.ATTACK_DAMAGE) : 2.0;
                return hp + (dmg * 4.5);
            }
        } catch (Exception ignored) {}
        return type.getCategory() == MobCategory.MONSTER ? 35.0 : 10.0;
    }

    public static LayerBoundary getLayerBoundaryForY(int y) {
        for (LayerBoundary b : LAYERS) {
            if (y >= b.minY() && y <= b.maxY()) return b;
        }
        return y > 2000 ? LAYERS.get(0) : LAYERS.get(LAYERS.size() - 1);
    }

    public static int getLayerForY(int y) {
        return getLayerBoundaryForY(y).id();
    }

    public static LayerContent getPool(int layer) {
        return LAYER_POOLS.getOrDefault(layer, new LayerContent());
    }

    /**
     * Cross-Mod Dynamic Spawn Replacement & Scaling:
     * Intercepts natural mob spawns in the Abyss and enforces the 20-mob pool for that specific layer,
     * while scaling HP and Attack Damage according to depth.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;

        LivingEntity entity = event.getEntity();
        BlockPos pos = entity.blockPosition();
        LayerBoundary boundary = getLayerBoundaryForY(pos.getY());
        LayerContent pool = getPool(boundary.id());

        if (!pool.mobs.isEmpty() && !pool.mobs.contains(entity.getType())) {
            // 65% chance to replace out-of-pool natural spawns with one of the 20 layer-selected cross-mod mobs
            if (serverLevel.random.nextFloat() < 0.65f) {
                EntityType<?> replacementType = pool.mobs.get(serverLevel.random.nextInt(pool.mobs.size()));
                Entity replacement = replacementType.create(serverLevel);
                if (replacement instanceof LivingEntity replacementMob) {
                    replacementMob.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                    scaleMobForLayer(replacementMob, boundary.mobStatMultiplier());
                    serverLevel.addFreshEntity(replacementMob);
                    event.setSpawnCancelled(true);
                    return;
                }
            }
        }

        // Scale original mob stats according to Abyss layer depth
        scaleMobForLayer(entity, boundary.mobStatMultiplier());
    }

    private static void scaleMobForLayer(LivingEntity mob, float multiplier) {
        if (multiplier <= 1.0f) return;
        var maxHp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            maxHp.setBaseValue(maxHp.getBaseValue() * multiplier);
            mob.setHealth(mob.getMaxHealth());
        }
        var attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(attack.getBaseValue() * Math.sqrt(multiplier));
        }
    }
}
