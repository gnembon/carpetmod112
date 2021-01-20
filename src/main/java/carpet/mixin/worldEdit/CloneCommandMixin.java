package carpet.mixin.worldEdit;

import carpet.mixin.accessors.StaticCloneDataAccessor;
import carpet.worldedit.WorldEditBridge;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.class_2010;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CloneCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

@Mixin(CloneCommand.class)
public class CloneCommandMixin {
    @Inject(method = "method_29272", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordRemove(MinecraftServer server, class_2010 sender, String[] args, CallbackInfo ci,
                              BlockPos pos1, BlockPos pos2, BlockPos pos3, BlockBox box1, BlockBox box2, boolean flag, Block block, Predicate<BlockState> predicate,
                              World world, boolean flag1, List<?> list, List<?> list1, List<?> list2, Deque<BlockPos> deque, BlockPos pos4, Iterator<BlockPos> it,
                              BlockPos currentPos, BlockEntity unused) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.getDefaultState(), null);
    }

    @Inject(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 3), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordAdd(MinecraftServer server, class_2010 sender, String[] args, CallbackInfo ci,
                           BlockPos blockpos, BlockPos blockpos1, BlockPos blockpos2, BlockBox structureboundingbox, BlockBox structureboundingbox1, int i, boolean flag, Block block, Predicate<?> predicate,
                           World world, boolean flag1, List<?> list, List<?> list1, List<?> list2, Deque<?> deque, BlockPos blockpos3, List<?> list3, List<?> list4, Iterator<?> var22,
                           @Coerce StaticCloneDataAccessor data) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, data.getPos(), data.getBlockState(), data.getNbt());
    }
}
