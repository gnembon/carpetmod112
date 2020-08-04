package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEndPortal.class)
public class BlockEndPortalMixin {
    @Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;changeDimension(I)Lnet/minecraft/entity/Entity;"))
    private void preChangeDimension(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason("Entity going through end portal");
    }

    @Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;changeDimension(I)Lnet/minecraft/entity/Entity;", shift = At.Shift.AFTER))
    private void postChangeDimension(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
