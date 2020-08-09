package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockTorch.class)
public class BlockTorchMixin {
    @Inject(method = "canPlaceOn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void relaxedBlockPlacement(World worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir, Block block) {
        // isTopSolid = true for lit pumpkin
        if (CarpetSettings.relaxedBlockPlacement && block == Blocks.LIT_PUMPKIN) cir.setReturnValue(true);
    }
}
