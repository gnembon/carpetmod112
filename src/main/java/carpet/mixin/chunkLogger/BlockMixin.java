package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void preRandomTick(World worldIn, BlockPos pos, BlockState state, Random random, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason(() -> "Randomtick block: " + state.getBlock().getTranslatedName());
    }

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void postRandomTick(World worldIn, BlockPos pos, BlockState state, Random random, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
