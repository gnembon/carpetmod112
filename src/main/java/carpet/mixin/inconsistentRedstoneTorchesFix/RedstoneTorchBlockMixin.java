package carpet.mixin.inconsistentRedstoneTorchesFix;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {
    @Inject(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26013(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"), cancellable = true)
    private void inconsistentRedstoneTorchesFix(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.inconsistentRedstoneTorchesFix && world.method_26012(pos, (RedstoneTorchBlock) (Object) this)) {
            ci.cancel();
        }
    }
}
