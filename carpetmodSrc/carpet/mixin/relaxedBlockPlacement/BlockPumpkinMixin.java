package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockPumpkin.class)
public class BlockPumpkinMixin {
    @Redirect(method = "canPlaceBlockAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;isTopSolid()Z"))
    private boolean allowMidAir(IBlockState state) {
        return CarpetSettings.relaxedBlockPlacement || state.isTopSolid();
    }
}
