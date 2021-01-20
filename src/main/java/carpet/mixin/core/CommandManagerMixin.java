package carpet.mixin.core;

import carpet.commands.CarpetCommands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void registerCarpetCommands(MinecraftServer serverIn, CallbackInfo ci) {
        CarpetCommands.register((CommandManager) (Object) this);
    }
}
