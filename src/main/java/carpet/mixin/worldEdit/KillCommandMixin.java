package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.class_2010;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KillCommand.class)
public class KillCommandMixin {
    @Redirect(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;kill()V"))
    private void recordEntityRemoval(Entity entity, MinecraftServer server, class_2010 sender, String[] args) {
        entity.kill();
        if (!(entity instanceof ServerPlayerEntity)) {
            ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
            WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.method_29608(), entity);
        }
    }
}
