package carpet.mixin.doubleTileTickSchedulingFix;

import carpet.CarpetSettings;
import carpet.helpers.ScheduledTickFix;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Redirect(method =  {
        "method_26012",
        "method_26076",
        "method_26014",
        "method_26077"
    }, at = @At(value = "NEW", target = "net/minecraft/world/ScheduledTick"))
    private ScheduledTick newNextTickListEntry(BlockPos pos, Block block) {
        return CarpetSettings.doubleTileTickSchedulingFix ? new ScheduledTickFix<>(pos, block) : new ScheduledTick(pos, block);
    }
}
