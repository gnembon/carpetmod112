package carpet.mixin.renewablePackedIce;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFallingBlock.class)
public abstract class EntityFallingBlockMixin extends Entity {
    @Shadow private IBlockState fallTile;
    private int iceCount;

    public EntityFallingBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 3), cancellable = true)
    private void checkIce(CallbackInfo ci) {
        Block block = fallTile.getBlock();
        BlockPos pos = new BlockPos(this);

        if (block == Blocks.ANVIL && CarpetSettings.renewablePackedIce &&
                this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.06, this.posZ)).getBlock() == Blocks.ICE) {
            if (iceCount < 2) {
                world.destroyBlock(pos.down(), false);
                this.onGround = false;
                iceCount++;
                ci.cancel();
            } else {
                world.setBlockState(pos.down(), Blocks.PACKED_ICE.getDefaultState(), 3);
                world.playEvent(2001, pos.down(), Block.getIdFromBlock(Blocks.PACKED_ICE));
            }
        }
    }
}
