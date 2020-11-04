package carpet.mixin.waypoints;

import carpet.CarpetSettings;
import carpet.utils.Waypoint;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandTP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandTP.class)
public abstract class CommandTPMixin extends CommandBase {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void teleportToWaypoint(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci) throws CommandException {
        if (args.length >= 1 && args.length <= 2 && CarpetSettings.commandWaypoint) {
            Entity entity = args.length == 1 ? getCommandSenderAsPlayer(sender) : getEntity(server, sender, args[0]);
            Waypoint waypoint = Waypoint.find(args[args.length - 1], (WorldServer) entity.world, server.worlds);
            if (waypoint != null) {
                waypoint.teleport(entity);
                notifyCommandListener(sender, this, "commands.tp.success", entity.getName(), waypoint.getFullName());
                ci.cancel();
            }
        }
    }
}
