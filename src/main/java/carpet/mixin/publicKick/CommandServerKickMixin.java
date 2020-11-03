package carpet.mixin.publicKick;

import carpet.CarpetSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandServerKick;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandServerKick.class)
public abstract class CommandServerKickMixin extends CommandBase {
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return CarpetSettings.publicKick || super.checkPermission(server, sender);
    }
}
