package carpet.mixin.silverFishDropGravel;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.InfestedBlock;
import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InfestedBlock.class)
public class InfestedBlockMixin extends Block {
    protected InfestedBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "onStacksDropped", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SilverfishEntity;playSpawnEffects()V", shift = At.Shift.AFTER))
    private void silverFishDropGravel(World world, BlockPos pos, BlockState state, float chance, int fortune, CallbackInfo ci) {
        // Silver fish will drop gravel when breaking out of a block. CARPET-XCOM
        if (CarpetSettings.silverFishDropGravel) dropStack(world, pos, new ItemStack(Blocks.GRAVEL));
    }
}
