package carpet.mixin.spongeRandom;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.SpongeBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin extends Block {
    @Shadow @Final public static BooleanProperty field_24805;

    protected SpongeBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void randomTick(World world, BlockPos pos, BlockState state, Random random) {
        super.randomTick(world, pos, state, random);
        if (!CarpetSettings.spongeRandom) {
            return;
        }
        boolean touchesWater = false;
        boolean touchesWet = false;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.offset(direction);
            if (world.getBlockState(neighbor).getMaterial() == Material.WATER) {
                touchesWater = true;
            }
            if (world.getBlockState(neighbor).getBlock() == Blocks.SPONGE && world.getBlockState(neighbor).get(field_24805)) {
                touchesWet = true;
            }
        }
        if (state.get(field_24805) && !touchesWater && world.method_26129(pos.up()) && world.isDay() && !world.hasRain(pos.up())) {
            world.setBlockState(pos, state.with(field_24805, Boolean.FALSE), 2);
        } else if (!state.get(field_24805) && (touchesWet || touchesWater || world.hasRain(pos.up())) && random.nextInt(3) == 0) {
            world.setBlockState(pos, state.with(field_24805, Boolean.TRUE), 2);
        }
    }
}
