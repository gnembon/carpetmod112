package carpet.mixin.renewablePackedIce;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    @Shadow private BlockState block;
    private int iceCount;

    public FallingBlockEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 3), cancellable = true)
    private void checkIce(CallbackInfo ci) {
        Block block = this.block.getBlock();
        BlockPos pos = new BlockPos(this);

        if (block == Blocks.ANVIL && CarpetSettings.renewablePackedIce &&
                this.world.getBlockState(new BlockPos(this.x, this.y - 0.06, this.z)).getBlock() == Blocks.ICE) {
            if (iceCount < 2) {
                world.breakBlock(pos.down(), false);
                this.onGround = false;
                iceCount++;
                ci.cancel();
            } else {
                world.setBlockState(pos.down(), Blocks.PACKED_ICE.getDefaultState(), 3);
                world.syncWorldEvent(2001, pos.down(), Block.getId(Blocks.PACKED_ICE));
            }
        }
    }
}
