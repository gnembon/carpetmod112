package carpet.mixin.railPowerLimit;

import carpet.CarpetSettings;
import net.minecraft.block.PoweredRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PoweredRailBlock.class)
public class PoweredRailBlockMixin {
    @ModifyConstant(method = "isPoweredByOtherRails(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;ZI)Z", constant = @Constant(intValue = 8))
    private int railPowerLimit(int limit) {
        // This is counting without the source, the value of the setting includes the source, hence -1
        return CarpetSettings.railPowerLimit - 1;
    }
}
