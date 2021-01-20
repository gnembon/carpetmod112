package carpet.mixin.calmNetherFires;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Random;

@NotThreadSafe
@Mixin(FireBlock.class)
public class FireBlockMixin {
    private boolean permanentFireBlock = false; // Thread safety: needs to be thread local for multi-threaded dimensions

    // @Redirect can't capture locals, so we need this @Inject to get the local
    @Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26013(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void rememberFlag(World worldIn, BlockPos pos, BlockState state, Random rand, CallbackInfo ci, Block block, boolean flag) {
        permanentFireBlock = flag;
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26013(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    private void scheduleUpdate(World world, BlockPos pos, Block block, int delay) {
        if (!CarpetSettings.calmNetherFires || !permanentFireBlock) world.method_26013(pos, block, delay);
    }
}
