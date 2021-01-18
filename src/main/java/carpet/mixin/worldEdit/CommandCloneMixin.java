package carpet.mixin.worldEdit;

import carpet.mixin.accessors.StaticCloneDataAccessor;
import carpet.worldedit.WorldEditBridge;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandClone;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

@Mixin(CommandClone.class)
public class CommandCloneMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordRemove(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci,
                              BlockPos pos1, BlockPos pos2, BlockPos pos3, StructureBoundingBox box1, StructureBoundingBox box2, boolean flag, Block block, Predicate<IBlockState> predicate,
                              World world, boolean flag1, List<?> list, List<?> list1, List<?> list2, Deque<BlockPos> deque, BlockPos pos4, Iterator<BlockPos> it,
                              BlockPos currentPos, TileEntity unused) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.getDefaultState(), null);
    }

    /*
    @Surrogate
    private void recordRemove(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci,
                              BlockPos pos1, BlockPos pos2, BlockPos pos3, StructureBoundingBox box1, StructureBoundingBox box2, int i, boolean flag, Block block, Predicate<IBlockState> predicate,
                              World world, boolean flag1, List<?> list, List<?> list1, List<?> list2, Deque<BlockPos> deque, BlockPos pos4, Iterator<BlockPos> it,
                              BlockPos currentPos) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.getDefaultState(), null);
    }
     */

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 3), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordAdd(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci,
                           BlockPos blockpos, BlockPos blockpos1, BlockPos blockpos2, StructureBoundingBox structureboundingbox, StructureBoundingBox structureboundingbox1, int i, boolean flag, Block block, Predicate<?> predicate,
                           World world, boolean flag1, List<?> list, List<?> list1, List<?> list2, Deque<?> deque, BlockPos blockpos3, List<?> list3, List<?> list4, Iterator<?> var22,
                           @Coerce StaticCloneDataAccessor data) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, data.getPos(), data.getBlockState(), data.getNbt());
    }
}
