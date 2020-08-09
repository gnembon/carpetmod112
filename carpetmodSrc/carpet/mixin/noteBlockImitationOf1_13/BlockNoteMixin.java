package carpet.mixin.noteBlockImitationOf1_13;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNote;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockNote.class)
public class BlockNoteMixin {
    private int previousInstrument;

    @Inject(method = "neighborChanged", at = @At(value = "RETURN"))
    private void onInstrumentChange(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        int instrument = getInstrumentId(world.getBlockState(pos.down()));
        if (previousInstrument != instrument) {
            previousInstrument = instrument;
            // Instrument change updates only observers
            if (CarpetSettings.noteBlockImitationOf1_13) world.updateObservingBlocksAt(pos, block);
        }
    }

    @Inject(method = "onBlockActivated", at = @At(value = "RETURN", ordinal = 1))
    private void onPitchChange(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        // Right click sends block updates and updates observers
        if(CarpetSettings.noteBlockImitationOf1_13) world.notifyNeighborsOfStateChange(pos, (BlockNote) (Object) this, true);
    }

    @Inject(method = "neighborChanged", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityNote;previousRedstoneState:Z"))
    private void onPowerChange(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        // Dual-edge redstone power change sends block updates and updates observers
        if(CarpetSettings.noteBlockImitationOf1_13) world.notifyNeighborsOfStateChange(pos, (BlockNote) (Object) this, true);
    }

    private static int getInstrumentId(IBlockState state) {
        Material material = state.getMaterial();
        if (material == Material.ROCK) return 1;
        if (material == Material.SAND) return 2;
        if (material == Material.GLASS) return 3;
        if (material == Material.WOOD) return 4;
        Block block = state.getBlock();
        if (block == Blocks.CLAY) return 5;
        if (block == Blocks.GOLD_BLOCK) return 6;
        if (block == Blocks.WOOL) return 7;
        if (block == Blocks.PACKED_ICE) return 8;
        if (block == Blocks.BONE_BLOCK) return 9;
        return 0;
    }
}
