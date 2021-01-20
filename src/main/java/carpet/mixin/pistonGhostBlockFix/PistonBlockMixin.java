package carpet.mixin.pistonGhostBlockFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPistonBlockEntityGhostBlockFix;
import carpet.utils.extensions.ExtendedServerWorldPistonGhostBlockFix;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Redirect(method = "method_27136", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;getId()I"))
    private int getFacingIndexGhostBlockFix(Direction facing, World world, BlockPos pos, BlockState state) {
        boolean suppressMove = false;
        if (CarpetSettings.pistonGhostBlocksFix == CarpetSettings.PistonGhostBlocksFix.clientAndServer) {
            BlockPos blockpos = new BlockPos(pos).method_31896(facing, 2);
            BlockState iblockstate = world.getBlockState(blockpos);

            if (iblockstate.getBlock() == Blocks.PISTON_EXTENSION) {
                final BlockEntity tileentity = world.getBlockEntity(blockpos);

                if (tileentity instanceof PistonBlockEntity) {
                    PistonBlockEntity te = (PistonBlockEntity) tileentity;
                    ExtendedPistonBlockEntityGhostBlockFix ext = (ExtendedPistonBlockEntityGhostBlockFix) te;
                    boolean facingMatch = te.getFacing() == facing;
                    boolean extending = te.method_27159();
                    boolean progressMatch = ext.getLastProgress() < 0.5F;
                    if (facingMatch && extending && progressMatch
                            && te.getWorld().getTime() == ext.getLastTicked()
                            && !((ExtendedServerWorldPistonGhostBlockFix) world).haveBlockActionsProcessed()) {
                        suppressMove = true;
                    }
                }
            }
        }
        return facing.getId() | (suppressMove ? 16 : 0);
    }

    @ModifyConstant(method = "onSyncedBlockEvent", constant = @Constant(intValue = 0, ordinal = 0), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;")
    ))
    private int setFlag1(int originalValue, BlockState state, World worldIn, BlockPos pos, int id, int param) {
        return CarpetSettings.pistonGhostBlocksFix == CarpetSettings.PistonGhostBlocksFix.clientAndServer && (param & 16) > 0 ? 1 : 0;
    }
}
