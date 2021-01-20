package carpet.mixin.movingBlockLightOptimization;

import carpet.helpers.PistonHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PistonExtensionBlock.class)
public abstract class PistonExtensionBlockMixin extends BlockWithEntity {
    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final public static EnumProperty<PistonHeadBlock.PistonType> TYPE;

    protected PistonExtensionBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    @Overwrite
    public StateManager appendProperties() {
        // Adding the properties to the list of allowed properties. CARPET-XCOM
        return new StateManager(this, FACING, TYPE, PistonHelper.OPACITY, PistonHelper.LIGHT);
    }

    // Grabbing the inherited properties from the parrent block that is moved and setting it to the moving block. CARPET-XCOM
    @Override
    public int getOpacity(BlockState state) {
        return state.get(PistonHelper.OPACITY);
    }

    @Override
    public int getLightLevel(BlockState state) {
        return state.get(PistonHelper.LIGHT);
    }
}
