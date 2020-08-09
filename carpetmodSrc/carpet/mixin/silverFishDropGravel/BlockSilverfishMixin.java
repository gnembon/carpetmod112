package carpet.mixin.silverFishDropGravel;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSilverfish.class)
public class BlockSilverfishMixin extends Block {
    protected BlockSilverfishMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntitySilverfish;spawnExplosionParticle()V", shift = At.Shift.AFTER))
    private void silverFishDropGravel(World world, BlockPos pos, IBlockState state, float chance, int fortune, CallbackInfo ci) {
        // Silver fish will drop gravel when breaking out of a block. CARPET-XCOM
        if (CarpetSettings.silverFishDropGravel) spawnAsEntity(world, pos, new ItemStack(Blocks.GRAVEL));
    }
}
