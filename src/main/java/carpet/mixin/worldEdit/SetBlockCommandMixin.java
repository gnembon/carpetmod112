package carpet.mixin.worldEdit;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_2010;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SetBlockCommand.class)
public class SetBlockCommandMixin {
    @Inject(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26083(Lnet/minecraft/util/math/BlockPos;Z)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforeDestroy(MinecraftServer server, class_2010 sender, String[] args, CallbackInfo ci, BlockPos pos, Block block, BlockState iblockstate, World world, CompoundTag tag, boolean flag) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        CompoundTag worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, Blocks.AIR.getDefaultState(), worldEditTag);
        CapturedDrops.setCapturingDrops(true);
    }

    @Inject(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26083(Lnet/minecraft/util/math/BlockPos;Z)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterDestroy(MinecraftServer server, class_2010 sender, String[] args, CallbackInfo ci, BlockPos blockpos, Block block, BlockState iblockstate, World world) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        CapturedDrops.setCapturingDrops(false);
        for (ItemEntity drop : CapturedDrops.getCapturedDrops())
            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
        CapturedDrops.clearCapturedDrops();
    }

    @Inject(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void normalSetBlock(MinecraftServer server, class_2010 sender, String[] args, CallbackInfo ci, BlockPos pos, Block block, BlockState blockState, World world, CompoundTag tag, boolean flag) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        CompoundTag worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, blockState, worldEditTag);
    }
}
