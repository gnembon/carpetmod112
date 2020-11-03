package carpet.mixin.worldEdit;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(CommandSetBlock.class)
public class CommandSetBlockMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforeDestroy(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos pos, Block block, IBlockState iblockstate, World world, NBTTagCompound tag, boolean flag) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        NBTTagCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, Blocks.AIR.getDefaultState(), worldEditTag);
        CapturedDrops.setCapturingDrops(true);
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterDestroy(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos blockpos, Block block, IBlockState iblockstate, World world) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        CapturedDrops.setCapturingDrops(false);
        for (EntityItem drop : CapturedDrops.getCapturedDrops())
            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
        CapturedDrops.clearCapturedDrops();
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void normalSetBlock(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos pos, Block block, IBlockState blockState, World world, NBTTagCompound tag, boolean flag) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        NBTTagCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, blockState, worldEditTag);
    }
}
