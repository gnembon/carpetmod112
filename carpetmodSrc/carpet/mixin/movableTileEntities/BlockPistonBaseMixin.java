package carpet.mixin.movableTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import carpet.utils.extensions.ExtendedTileEntityPistonMTE;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin {
    @Redirect(method = "canPush", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasTileEntity()Z"))
    private static boolean isImmovableTileEntity(Block block) {
        if (!block.hasTileEntity()) return false;
        if (CarpetSettings.movableTileEntities) return !PistonHelper.isPushableTileEntityBlock(block);
        return !(block instanceof BlockWorkbench);
    }

    private static final ThreadLocal<List<TileEntity>> movedTileEntities = new ThreadLocal<>();
    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void createTileEntityList(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir, BlockPistonStructureHelper helper, List<BlockPos> positions) {
        if(CarpetSettings.movableTileEntities || CarpetSettings.autocrafter){
            List<TileEntity> tileEntities = new ArrayList<>();
            for (BlockPos blockpos : positions) {
                TileEntity tileentity = worldIn.getTileEntity(blockpos);
                tileEntities.add(tileentity);

                if (tileentity != null) {
                    worldIn.removeTileEntity(blockpos);
                    tileentity.markDirty();
                }
            }
            movedTileEntities.set(tileEntities);
        }
    }

    // Redirect can't local-capture so we need redirect to nothing and inject

    @Redirect(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockPistonMoving;createTilePiston(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;ZZ)Lnet/minecraft/tileentity/TileEntity;", ordinal = 0))
    private TileEntity dontCreateTilePiston(IBlockState blockState, EnumFacing facing, boolean extending, boolean shouldHeadBeRendered) { return null; }
    @Redirect(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", ordinal = 0))
    private void dontAddTileEntity(World world, BlockPos pos, TileEntity tileEntity) {}

    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void handleMovableTileEntity(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
             BlockPistonStructureHelper helper, List<BlockPos> positions, List<IBlockState> states, List<BlockPos> list2, int k, IBlockState[] aiblockstate, EnumFacing enumfacing, int index, BlockPos currentPos, IBlockState currentState) {
        TileEntity tilePiston = BlockPistonMoving.createTilePiston(states.get(index), direction, extending, false);
        if (CarpetSettings.autocrafter && currentState instanceof BlockWorkbench || CarpetSettings.movableTileEntities && PistonHelper.isPushableTileEntityBlock(currentState.getBlock())) {
            ((ExtendedTileEntityPistonMTE) tilePiston).setCarriedTileEntity(movedTileEntities.get().get(index));
        }
        worldIn.setTileEntity(currentPos, tilePiston);
    }
}
