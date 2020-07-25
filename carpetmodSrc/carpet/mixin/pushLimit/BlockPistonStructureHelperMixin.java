package carpet.mixin.pushLimit;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockPistonStructureHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockPistonStructureHelper.class)
public class BlockPistonStructureHelperMixin {
    @ModifyConstant(method = "addBlockLine", constant = @Constant(intValue = 12))
    private int pushLimit(int original) {
        return CarpetSettings.pushLimit;
    }
}
