package carpet.mixin.desertShrubs;

import carpet.CarpetSettings;
import carpet.helpers.BlockSaplingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin {
    @Inject(method = "method_26789", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/SaplingBlock;method_26790(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Ljava/util/Random;)V"), cancellable = true)
    private void desertShrubs(World world, BlockPos pos, BlockState state, Random rand, CallbackInfo ci) {
        if (CarpetSettings.desertShrubs && world.method_26188(pos) == Biomes.DESERT && !BlockSaplingHelper.hasWater(world, pos)) {
            world.method_26019(pos, Blocks.DEADBUSH.getDefaultState());
            ci.cancel();
        }
    }
}
