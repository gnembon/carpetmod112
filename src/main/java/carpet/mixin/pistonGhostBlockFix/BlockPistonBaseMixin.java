package carpet.mixin.pistonGhostBlockFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedTileEntityPistonGhostBlockFix;
import carpet.utils.extensions.ExtendedWorldServerPistonGhostBlockFix;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin {
    @Redirect(method = "checkForMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;getIndex()I"))
    private int getFacingIndexGhostBlockFix(EnumFacing facing, World world, BlockPos pos, IBlockState state) {
        boolean suppressMove = false;
        if (CarpetSettings.pistonGhostBlocksFix == CarpetSettings.PistonGhostBlocksFix.clientAndServer) {
            BlockPos blockpos = new BlockPos(pos).offset(facing, 2);
            IBlockState iblockstate = world.getBlockState(blockpos);

            if (iblockstate.getBlock() == Blocks.PISTON_EXTENSION) {
                final TileEntity tileentity = world.getTileEntity(blockpos);

                if (tileentity instanceof TileEntityPiston) {
                    TileEntityPiston te = (TileEntityPiston) tileentity;
                    ExtendedTileEntityPistonGhostBlockFix ext = (ExtendedTileEntityPistonGhostBlockFix) te;
                    boolean facingMatch = te.getFacing() == facing;
                    boolean extending = te.isExtending();
                    boolean progressMatch = ext.getLastProgress() < 0.5F;
                    if (facingMatch && extending && progressMatch
                            && te.getWorld().getTotalWorldTime() == ext.getLastTicked()
                            && !((ExtendedWorldServerPistonGhostBlockFix) world).haveBlockActionsProcessed()) {
                        suppressMove = true;
                    }
                }
            }
        }
        return facing.getIndex() | (suppressMove ? 16 : 0);
    }

    @ModifyConstant(method = "eventReceived", constant = @Constant(intValue = 0, ordinal = 0), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;")
    ))
    private int setFlag1(int originalValue, IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        return CarpetSettings.pistonGhostBlocksFix == CarpetSettings.PistonGhostBlocksFix.clientAndServer && (param & 16) > 0 ? 1 : 0;
    }
}
