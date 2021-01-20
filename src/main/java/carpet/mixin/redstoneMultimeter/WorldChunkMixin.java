package carpet.mixin.redstoneMultimeter;

import carpet.CarpetSettings;
import narcolepticfrog.rsmm.events.StateChangeEventDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Shadow @Final private World world;

    @Inject(method = "method_27373", at = @At("TAIL"))
    private void onSetBlockState(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        if (CarpetSettings.redstoneMultimeter) StateChangeEventDispatcher.dispatchEvent(world, pos);
    }
}
