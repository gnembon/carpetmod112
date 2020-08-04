package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockDeadBush.class)
public class BlockDeadBushMixin {
    @Redirect(method = "harvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getBlockStats(Lnet/minecraft/block/Block;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addBlockMeta(Block blockIn, World worldIn, EntityPlayer player, BlockPos pos, IBlockState state) {
        return StatHelper.getBlockStateStats(state);
    }
}
