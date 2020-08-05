package carpet.mixin.fillUpdates;

import carpet.utils.extensions.ExtendedChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements ExtendedChunk {
    @Shadow @Nullable public abstract IBlockState setBlockState(BlockPos pos, IBlockState state);

    private boolean skipUpdates;

    @Override
    public IBlockState setBlockStateCarpet(BlockPos pos, IBlockState state, boolean skipUpdates) {
        try {
            this.skipUpdates = skipUpdates;
            return this.setBlockState(pos, state);
        } finally {
            this.skipUpdates = false;
        }
    }

    @Redirect(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z"))
    private boolean skipUpdates(World world) {
        return skipUpdates || world.isRemote;
    }
}
