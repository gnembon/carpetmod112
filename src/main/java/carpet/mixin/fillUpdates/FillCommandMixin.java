package carpet.mixin.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.server.command.FillCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FillCommand.class)
public class FillCommandMixin {
    @ModifyConstant(method = "method_29272", constant = @Constant(intValue = 2))
    private int changeFlags(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 128);
    }

    @Redirect(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26017(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Z)V"))
    private void notifyNeighbors(World world, BlockPos pos, Block blockType, boolean updateObservers) {
        if (!CarpetSettings.fillUpdates) return;
        world.method_26017(pos, blockType, updateObservers);
    }
}
