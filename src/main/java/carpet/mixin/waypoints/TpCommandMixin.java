package carpet.mixin.waypoints;

import carpet.CarpetSettings;
import carpet.utils.Waypoint;
import net.minecraft.class_1999;
import net.minecraft.class_6170;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_6170.class)
public abstract class TpCommandMixin extends class_1999 {
    @Inject(method = "method_29272", at = @At("HEAD"), cancellable = true)
    private void teleportToWaypoint(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci) throws CommandException {
        if (args.length >= 1 && args.length <= 2 && CarpetSettings.commandWaypoint) {
            Entity entity = args.length == 1 ? method_28708(sender) : method_28743(server, sender, args[0]);
            Waypoint waypoint = Waypoint.find(args[args.length - 1], (ServerWorld) entity.world, server.worlds);
            if (waypoint != null) {
                waypoint.teleport(entity);
                method_28710(sender, this, "commands.tp.success", entity.getName(), waypoint.getFullName());
                ci.cancel();
            }
        }
    }
}
