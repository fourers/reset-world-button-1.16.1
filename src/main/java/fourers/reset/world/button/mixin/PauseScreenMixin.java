package fourers.reset.world.button.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.WorldData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
	private static final String MOD_ID = "reset-world-button";
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    protected PauseScreenMixin() {
        super(new TextComponent("Pause"));
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    private void addResetWorldButton(CallbackInfo ci) {
        this.addButton(new Button(
                this.width / 2 - 102,
                this.height / 4 + 136,
                204,
                20,
                new TextComponent("Reset World"),
                button -> showConfirmScreen()
        ));
    }

    private void showConfirmScreen() {
        Minecraft.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        resetWorld();
                    } else {
                        Minecraft.getInstance().setScreen((Screen)(Object)this);
                    }
                },
                new TextComponent("Reset World?"),
                new TextComponent("This will delete ALL world data and regenerate the world with the same seed. This cannot be undone!"),
                new TranslatableComponent("gui.yes"),
                new TranslatableComponent("gui.no")
        ));
    }

    private void resetWorld() {
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
            mc.setScreen(new TitleScreen());
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

    private void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
