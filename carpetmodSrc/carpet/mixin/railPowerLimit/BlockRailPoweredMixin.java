package carpet.mixin.railPowerLimit;

import carpet.CarpetSettings;
import net.minecraft.block.BlockRailPowered;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockRailPowered.class)
public class BlockRailPoweredMixin {
    @ModifyConstant(method = "findPoweredRailSignal", constant = @Constant(intValue = 8))
    private int railPowerLimit(int limit) {
        // This is counting without the source, the value of the setting includes the source, hence -1
        return CarpetSettings.railPowerLimit - 1;
    }
}
