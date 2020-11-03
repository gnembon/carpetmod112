package carpet.mixin.doubleRetraction;

import carpet.CarpetSettings;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin {
    @Shadow @Final public static PropertyBool EXTENDED;

    @Inject(method = "checkForMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1))
    private void onRetract(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (CarpetSettings.doubleRetraction) {
            worldIn.setBlockState(pos, state.withProperty(EXTENDED, false), 2);
        }
    }
}
