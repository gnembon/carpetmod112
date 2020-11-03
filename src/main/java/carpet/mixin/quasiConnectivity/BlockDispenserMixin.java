package carpet.mixin.quasiConnectivity;

import carpet.CarpetSettings;
import net.minecraft.block.BlockDispenser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockDispenser.class)
public class BlockDispenserMixin {
    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockPowered(Lnet/minecraft/util/math/BlockPos;)Z", ordinal = 1))
    private boolean isBlockAbovePowered(World world, BlockPos pos) {
        return CarpetSettings.quasiConnectivity && world.isBlockPowered(pos);
    }
}
