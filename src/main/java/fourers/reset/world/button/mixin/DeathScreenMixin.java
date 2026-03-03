package fourers.reset.world.button.mixin;

import fourers.reset.world.button.core.ResetWorldHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

    @Unique
    private Button retryButton;

    @Unique
    private Button newSeedButton;

    protected DeathScreenMixin(TextComponent title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addResetButton(CallbackInfo ci) {

        // Add your custom button
        retryButton = this.addButton(new Button(
            this.width / 2 - 100,
            this.height / 4 + 120,
            200,
            20,
            new TextComponent("Reset World"),
            button -> {
                ResetWorldHandler.queueRetry();
            }
        ));

        retryButton.active = false; // start disabled

        newSeedButton = this.addButton(new Button(
            this.width / 2 - 100,
            this.height / 4 + 144,
            200,
            20,
            new TextComponent("Reset New Seed"),
            button -> {
                ResetWorldHandler.queueNewSeed();
            }
        ));

        newSeedButton.active = false;
    }
}
