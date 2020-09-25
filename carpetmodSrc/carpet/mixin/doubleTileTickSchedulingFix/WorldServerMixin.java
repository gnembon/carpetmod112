package carpet.mixin.doubleTileTickSchedulingFix;

import carpet.CarpetSettings;
import carpet.helpers.NextTickListEntryFix;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Redirect(method =  {
        "isBlockTickPending",
        "isUpdateScheduled",
        "updateBlockTick",
        "scheduleBlockUpdate"
    }, at = @At(value = "NEW", target = "net/minecraft/world/NextTickListEntry"))
    private NextTickListEntry newNextTickListEntry(BlockPos pos, Block block) {
        return CarpetSettings.doubleTileTickSchedulingFix ? new NextTickListEntryFix<>(pos, block) : new NextTickListEntry(pos, block);
    }
}
