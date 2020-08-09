package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockFenceGate.class)
public class BlockFenceGateMixin extends BlockHorizontal {
    protected BlockFenceGateMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "canPlaceBlockAt", at = @At("HEAD"), cancellable = true)
    private void allowMidAir(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.relaxedBlockPlacement) {
            cir.setReturnValue(super.canPlaceBlockAt(world, pos));
        }
    }
}
