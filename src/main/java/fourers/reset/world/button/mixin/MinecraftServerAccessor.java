package fourers.reset.world.button.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor("registryHolder")
    RegistryAccess.RegistryHolder getRegistryHolder();
}
