package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.CommandKill;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandKill.class)
public class CommandKillMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onKillCommand()V"))
    private void recordEntityRemoval(Entity entity, MinecraftServer server, ICommandSender sender, String[] args) {
        entity.onKillCommand();
        if (!(entity instanceof EntityPlayerMP)) {
            EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
            WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getEntityWorld(), entity);
        }
    }
}
