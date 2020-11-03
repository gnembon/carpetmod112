package carpet.mixin.core;

import carpet.CarpetServer;
import carpet.commands.CarpetCommands;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandManager.class)
public class ServerCommandManagerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void registerCarpetCommands(MinecraftServer serverIn, CallbackInfo ci) {
        CarpetCommands.register((ServerCommandManager) (Object) this);
    }
}
