package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBlockData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CommandBlockData.class)
public class CommandBlockDataMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordBlockEdit(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci,
                                 BlockPos pos, World world, IBlockState state, TileEntity te, NBTTagCompound tag) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, state, tag);
    }
}
