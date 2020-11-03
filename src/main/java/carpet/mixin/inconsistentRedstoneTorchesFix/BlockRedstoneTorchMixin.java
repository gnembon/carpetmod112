package carpet.mixin.inconsistentRedstoneTorchesFix;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRedstoneTorch.class)
public class BlockRedstoneTorchMixin {
    @Inject(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"), cancellable = true)
    private void inconsistentRedstoneTorchesFix(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.inconsistentRedstoneTorchesFix && world.isBlockTickPending(pos, (BlockRedstoneTorch) (Object) this)) {
            ci.cancel();
        }
    }
}
