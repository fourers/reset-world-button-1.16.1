package fourers.reset.world.button;

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

public class ResetWorldHandler {
    private static final String MOD_ID = "reset-world-button";
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static boolean pendingReset = false;

    public static void queueRetry() {
        pendingReset = true;
    }

    public static void clientTick() {
        // Use client tick to reset world if game is still running in background
        if (pendingReset) {
            pendingReset = false;
            resetWorld();
        }
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
