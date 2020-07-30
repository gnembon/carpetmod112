package carpet.mixin.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.command.CommandClone;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandClone.class)
public class CommandCloneMixin {
    @ModifyConstant(method = "execute", constant = {@Constant(intValue = 2), @Constant(intValue = 3)})
    private int changeFlags(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 128);
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;notifyNeighborsRespectDebug(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Z)V"))
    private void notifyNeighbors(World world, BlockPos pos, Block blockType, boolean updateObservers) {
        if (!CarpetSettings.fillUpdates) return;
        world.notifyNeighborsRespectDebug(pos, blockType, updateObservers);
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleBlockUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"))
    private void scheduleBlockUpdate(World world, BlockPos pos, Block blockIn, int delay, int priority) {
        if (!CarpetSettings.fillUpdates) return;
        world.scheduleBlockUpdate(pos, blockIn, delay, priority);
    }
}
