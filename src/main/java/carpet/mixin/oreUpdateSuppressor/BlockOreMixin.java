package carpet.mixin.oreUpdateSuppressor;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockOre.class)
public class BlockOreMixin extends Block {
    public BlockOreMixin(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (CarpetSettings.oreUpdateSuppressor && this == Blocks.EMERALD_ORE){
            throw new StackOverflowError("Carpet-triggered update suppression");
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }
}
