package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandSummon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CommandSummon.class)
public class CommandSummonMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/server/CommandSummon;notifyCommandListener(Lnet/minecraft/command/ICommandSender;Lnet/minecraft/command/ICommand;Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordEntityCreation(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, String s, BlockPos blockpos, Vec3d vec3d, double d0, double d1, double d2, World world, NBTTagCompound nbttagcompound, boolean flag, Entity entity) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordEntityCreation(worldEditPlayer, world, entity);
    }
}
