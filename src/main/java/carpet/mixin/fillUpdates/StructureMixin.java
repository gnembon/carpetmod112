package carpet.mixin.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Structure.class)
public class StructureMixin {
    @ModifyConstant(method = "method_28024", constant = @Constant(intValue = 4))
    private int changeFlags1(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 128);
    }

    @ModifyArg(method = "method_28024", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 1), index = 2)
    private int changeFlags2(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 128);
    }

    @Redirect(method = "method_28024", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26017(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Z)V"))
    private void notifyNeighbors(World world, BlockPos pos, Block blockType, boolean updateObservers) {
        if (!CarpetSettings.fillUpdates) return;
        world.method_26017(pos, blockType, updateObservers);
    }
}
