package carpet.mixin.worldEdit;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandFill;
import net.minecraft.command.ICommandSender;
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

@Mixin(CommandFill.class)
public class CommandFillMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforeDestroy(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1, Block block, IBlockState iblockstate, BlockPos blockpos2, BlockPos blockpos3, int i, World world, NBTTagCompound tag, boolean flag, List<?> list, int l, int i1, int j1, BlockPos currentPos) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        NBTTagCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.getDefaultState(), worldEditTag);
        CapturedDrops.setCapturingDrops(true);
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterDestroy(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1, Block block, IBlockState iblockstate, BlockPos blockpos2, BlockPos blockpos3, int i, World world) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        CapturedDrops.setCapturingDrops(false);
        for (EntityItem drop : CapturedDrops.getCapturedDrops())
            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
        CapturedDrops.clearCapturedDrops();
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void hollowSetBlock(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1, Block block, IBlockState iblockstate, BlockPos blockpos2, BlockPos blockpos3, int i, World world, NBTTagCompound tag, boolean flag, List<?> list, int l, int i1, int j1, BlockPos currentPos) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        NBTTagCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.getDefaultState(), worldEditTag);
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void normalSetBlock(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1, Block block, IBlockState blockState, BlockPos blockpos2, BlockPos blockpos3, int i, World world, NBTTagCompound tag, boolean flag, List<?> list, int l, int i1, int j1, BlockPos currentPos) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        NBTTagCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, blockState, worldEditTag);
    }
}
