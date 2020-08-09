package carpet.mixin.desertShrubs;

import carpet.CarpetSettings;
import carpet.helpers.BlockSaplingHelper;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockSapling.class)
public class BlockSaplingMixin {
    @Inject(method = "grow(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockSapling;generateTree(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"), cancellable = true)
    private void desertShrubs(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (CarpetSettings.desertShrubs && world.getBiome(pos) == Biomes.DESERT && !BlockSaplingHelper.hasWater(world, pos)) {
            world.setBlockState(pos, Blocks.DEADBUSH.getDefaultState());
            ci.cancel();
        }
    }
}
