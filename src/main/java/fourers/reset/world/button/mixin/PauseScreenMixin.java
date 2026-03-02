package fourers.reset.world.button.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fourers.reset.world.button.ResetWorldHandler;

@Environment(EnvType.CLIENT)
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

    protected PauseScreenMixin() {
        super(new TextComponent("Pause"));
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    private void addResetWorldButton(CallbackInfo ci) {
        this.addButton(new Button(
                this.width / 2 - 102,
                this.height / 4 + 144 - 16,
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
                        ResetWorldHandler.resetWorld();
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
}
