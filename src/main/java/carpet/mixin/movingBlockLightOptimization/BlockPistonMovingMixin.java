package carpet.mixin.movingBlockLightOptimization;

import carpet.helpers.PistonHelper;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPistonMoving.class)
public abstract class BlockPistonMovingMixin extends BlockContainer {
    @Shadow @Final public static PropertyDirection FACING;
    @Shadow @Final public static PropertyEnum<BlockPistonExtension.EnumPistonType> TYPE;

    protected BlockPistonMovingMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    @Overwrite
    public BlockStateContainer createBlockState() {
        // Adding the properties to the list of allowed properties. CARPET-XCOM
        return new BlockStateContainer(this, FACING, TYPE, PistonHelper.OPACITY, PistonHelper.LIGHT);
    }

    // Grabbing the inherited properties from the parrent block that is moved and setting it to the moving block. CARPET-XCOM
    @Override
    public int getLightOpacity(IBlockState state) {
        return state.getValue(PistonHelper.OPACITY);
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(PistonHelper.LIGHT);
    }
}
