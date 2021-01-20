package carpet.mixin.movableTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import carpet.utils.extensions.ExtendedPistonBlockEntityMBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Redirect(method = "isMovable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasBlockEntity()Z"))
    private static boolean isImmovableTileEntity(Block block) {
        if (!block.hasBlockEntity()) return false;
        if (CarpetSettings.movableTileEntities) return !PistonHelper.isPushableTileEntityBlock(block);
        return !(block instanceof CraftingTableBlock);
    }

    private static final ThreadLocal<List<BlockEntity>> movedTileEntities = new ThreadLocal<>();
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void createTileEntityList(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir, PistonHandler helper, List<BlockPos> positions) {
        if(CarpetSettings.movableTileEntities || CarpetSettings.autocrafter){
            List<BlockEntity> tileEntities = new ArrayList<>();
            for (BlockPos blockpos : positions) {
                BlockEntity tileentity = worldIn.getBlockEntity(blockpos);
                tileEntities.add(tileentity);

                if (tileentity != null) {
                    worldIn.removeBlockEntity(blockpos);
                    tileentity.markDirty();
                }
            }
            movedTileEntities.set(tileEntities);
        }
    }

    // Redirect can't local-capture so we need redirect to nothing and inject

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PistonExtensionBlock;createBlockEntityPiston(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;ZZ)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 0))
    private BlockEntity dontCreateTilePiston(BlockState blockState, Direction facing, boolean extending, boolean shouldHeadBeRendered) { return null; }
    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V", ordinal = 0))
    private void dontAddTileEntity(World world, BlockPos pos, BlockEntity tileEntity) {}

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void handleMovableTileEntity(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
             PistonHandler helper, List<BlockPos> positions, List<BlockState> states, List<BlockPos> list2, int k, BlockState[] aiblockstate, Direction enumfacing, int index, BlockPos currentPos, BlockState currentState) {
        BlockEntity tilePiston = PistonExtensionBlock.createBlockEntityPiston(states.get(index), direction, extending, false);
        if (CarpetSettings.autocrafter && currentState instanceof CraftingTableBlock || CarpetSettings.movableTileEntities && PistonHelper.isPushableTileEntityBlock(currentState.getBlock())) {
            ((ExtendedPistonBlockEntityMBE) tilePiston).setCarriedTileEntity(movedTileEntities.get().get(index));
        }
        worldIn.setBlockEntity(currentPos, tilePiston);
    }
}
