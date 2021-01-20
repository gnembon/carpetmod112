package carpet.mixin.waterFlow;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Random;

@NotThreadSafe
@Mixin(FlowingFluidBlock.class)
public class FlowingFluidBlockMixin {
    private int level = 8; // Thread safety: needs to be thread local for multi-threaded dimensions

    @Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FlowingFluidBlock;method_26597(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/util/Set;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void rememberLevel(World worldIn, BlockPos pos, BlockState state, Random rand, CallbackInfo ci, int i) {
        level = i;
    }

    @ModifyConstant(method = "method_26597", constant = @Constant(intValue = 1))
    private int maxFlow(int flow) {
        if (CarpetSettings.waterFlow == CarpetSettings.WaterFlow.vanilla) return flow;
        int level = this.level;
        if (level >= 8) return flow;
        return Math.max(level - (CarpetSettings.waterFlow == CarpetSettings.WaterFlow.correct ? 1 : 3), 1);
    }
}
