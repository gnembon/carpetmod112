package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TorchBlock.class)
public class TorchBlockMixin {
    @Inject(method = "method_26847", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void relaxedBlockPlacement(World worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir, Block block) {
        // isTopSolid = true for lit pumpkin
        if (CarpetSettings.relaxedBlockPlacement && block == Blocks.LIT_PUMPKIN) cir.setReturnValue(true);
    }
}
