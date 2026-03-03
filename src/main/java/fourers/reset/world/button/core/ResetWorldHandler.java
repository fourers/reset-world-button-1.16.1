package fourers.reset.world.button.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.WorldData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.DynamicOps;

import fourers.reset.world.button.mixin.MinecraftServerAccessor;

public class ResetWorldHandler {
    private static final String MOD_ID = "reset-world-button";
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static boolean pendingReset = false;
    private static boolean newSeed = false;

    public static void queueRetry() {
        pendingReset = true;
        newSeed = false;
    }

    public static void queueNewSeed() {
        pendingReset = true;
        newSeed = true;
    }

    public static void clientTick() {
        // Use client tick to reset world if game is still running in background
        if (pendingReset) {
            boolean isNewSeed = newSeed;
            pendingReset = false;
            newSeed = false;

            resetWorld(isNewSeed);
        }
    }

    public static void resetWorld(boolean isNewSeed) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.hasSingleplayerServer()) {
            LOGGER.warn("Not in a singleplayer world — cannot reset.");
            return;
        }

        MinecraftServer server = mc.getSingleplayerServer();

        Path worldDir = server.getWorldPath(LevelResource.ROOT);
        String levelId = getLevelId(worldDir);

        WorldData worldData = server.getWorldData();
        LevelSettings levelSettings = worldData.getLevelSettings();
        WorldGenSettings currentWorldGenSettings = worldData.worldGenSettings();

        WorldGenSettings worldGenSettings;
        if (isNewSeed) {
            worldGenSettings = randomiseWorldSeed(currentWorldGenSettings);
        } else {
            worldGenSettings = currentWorldGenSettings;
        }

        RegistryAccess.RegistryHolder registryHolder = ((MinecraftServerAccessor) server).getRegistryHolder();

        LOGGER.info("Will reset world: {}", levelId);
        mc.level.disconnect();

        mc.execute(() -> {
            try {
                LOGGER.info("Resetting world: {}", levelId);
                deleteDir(worldDir);

                LOGGER.info("Recreating world: {}", levelId);
                mc.createLevel(levelId, levelSettings, registryHolder, worldGenSettings);
            } catch (Exception e) {
                LOGGER.error("Failed to reset world", e);
            }
        });
    }

    private static String getLevelId(Path worldDir) {
		return worldDir.toAbsolutePath().getParent().getFileName().toString();
    }

    private static WorldGenSettings randomiseWorldSeed(WorldGenSettings worldGenSettings) {
        long seed = worldGenSettings.seed();
        long newSeed = new Random().nextLong();

        LOGGER.info("Randomising seed from '{}' to {}", seed, newSeed);
        return new WorldGenSettings(newSeed, worldGenSettings.generateFeatures(), worldGenSettings.generateBonusChest(), worldGenSettings.dimensions());
    }

    private static void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
