package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockDoublePlant.class)
public class BlockDoublePlantMixin {
    @Redirect(method = "onHarvest", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getBlockStats(Lnet/minecraft/block/Block;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addBlockMeta(Block blockIn, World world, BlockPos pos, IBlockState state) {
        return StatHelper.getBlockStateStats(state);
    }
}
