package carpet.mixin.redstoneMultimeter;

import carpet.CarpetSettings;
import narcolepticfrog.rsmm.events.PistonPushEventDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onPush(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                        PistonHandler helper, List<BlockPos> positions, List<BlockState> states, List<BlockPos> list2, int k, BlockState[] aiblockstate, Direction movementDirection, int index, BlockPos currentPos) {
        if (CarpetSettings.redstoneMultimeter) {
            PistonPushEventDispatcher.dispatchEvent(worldIn, currentPos, movementDirection);
        }
    }
}
