package carpet.mixin.randomTickOptimization;

import carpet.helpers.RandomTickOptimization;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    // Prevent execution of the original return
    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;requiresUpdates()Z"))
    private boolean requiresUpdatesWorldGenFix(Block block) {
        return !RandomTickOptimization.needsWorldGenFix && block.requiresUpdates();
    }

    @Inject(method = "updateBlockTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;requiresUpdates()Z"), cancellable = true)
    private void randomTickWorldGenFix(BlockPos pos, Block blockIn, int delay, int priority, CallbackInfo ci) {
        if (!RandomTickOptimization.needsWorldGenFix) return;
        if (this.isAreaLoaded(pos.add(-8, -8, -8), pos.add(8, 8, 8))) {
            IBlockState state = this.getBlockState(pos);
            if (state.getMaterial() != Material.AIR && state.getBlock() == blockIn) {
                state.getBlock().updateTick(this, pos, state, this.rand);
            }
            ci.cancel(); // move return into the if for `needsWorldGenFix`
        }
    }
}
