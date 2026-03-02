package fourers.reset.world.button;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.WorldData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResetWorldButton implements ModInitializer {
	private static final String MOD_ID = "reset-world-button";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Loading Reset World Button...");
	}

	public static void resetWorld() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hasSingleplayerServer()) {
			MinecraftServer server = mc.getSingleplayerServer();

			// Capture the level ID before disconnecting
			Path worldDir = server.getWorldPath(LevelResource.ROOT);
			String levelId = worldDir.toAbsolutePath().getParent().getFileName().toString();

            WorldData worldData = server.getWorldData();
            LevelSettings lockedLevelSettings = worldData.getLevelSettings();
            WorldGenSettings lockedGenSettings = worldData.worldGenSettings();

            RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();

			LOGGER.info("Will reset world: {}", levelId);
            mc.level.disconnect();

            mc.execute(() -> {
                try {
					LOGGER.info("Resetting world: {}", levelId);
                    deleteDir(worldDir);

                    LOGGER.info("Recreating world: {}", levelId);
					mc.createLevel(levelId, lockedLevelSettings, registryHolder, lockedGenSettings);
                } catch (Exception e) {
                    LOGGER.error("Failed to reset world", e);
                }
            });
        } else {
            LOGGER.warn("Not in a singleplayer world — cannot reset.");
        }
    }

    private static void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
