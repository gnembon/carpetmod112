package carpet.mixin.spongeRandom;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSponge;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(BlockSponge.class)
public class BlockSpongeMixin extends Block {
    @Shadow @Final public static PropertyBool WET;

    protected BlockSpongeMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.randomTick(world, pos, state, random);
        if (!CarpetSettings.spongeRandom) {
            return;
        }
        boolean touchesWater = false;
        boolean touchesWet = false;
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(direction);
            if (world.getBlockState(neighbor).getMaterial() == Material.WATER) {
                touchesWater = true;
            }
            if (world.getBlockState(neighbor).getBlock() == Blocks.SPONGE && world.getBlockState(neighbor).getValue(WET)) {
                touchesWet = true;
            }
        }
        if (state.getValue(WET) && !touchesWater && world.canSeeSky(pos.up()) && world.isDaytime() && !world.isRainingAt(pos.up())) {
            world.setBlockState(pos, state.withProperty(WET, Boolean.FALSE), 2);
        } else if (!state.getValue(WET) && (touchesWet || touchesWater || world.isRainingAt(pos.up())) && random.nextInt(3) == 0) {
            world.setBlockState(pos, state.withProperty(WET, Boolean.TRUE), 2);
        }
    }
}
