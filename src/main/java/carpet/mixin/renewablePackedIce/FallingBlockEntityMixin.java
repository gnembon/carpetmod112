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
    @Shadow private BlockState field_22245;
    private int iceCount;

    public FallingBlockEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 3), cancellable = true)
    private void checkIce(CallbackInfo ci) {
        Block block = field_22245.getBlock();
        BlockPos pos = new BlockPos(this);

        if (block == Blocks.ANVIL && CarpetSettings.renewablePackedIce &&
                this.world.getBlockState(new BlockPos(this.field_33071, this.field_33072 - 0.06, this.field_33073)).getBlock() == Blocks.ICE) {
            if (iceCount < 2) {
                world.method_26083(pos.method_31898(), false);
                this.onGround = false;
                iceCount++;
                ci.cancel();
            } else {
                world.setBlockState(pos.method_31898(), Blocks.PACKED_ICE.getDefaultState(), 3);
                world.method_26069(2001, pos.method_31898(), Block.getId(Blocks.PACKED_ICE));
            }
        }
    }
}
