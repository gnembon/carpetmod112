package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    Block.class,
    BlockWithEntity.class,
    DeadBushBlock.class,
    IceBlock.class,
    Leaves2Block.class,
    LeavesBlock.class,
    SnowLayerBlock.class,
    TallGrassBlock.class,
    VineBlock.class,
    CobwebBlock.class
})
public class BlockHarvestMixins {
    @Redirect(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33886(Lnet/minecraft/block/Block;)Lnet/minecraft/stat/Stat;"))
    private Stat addBlockMeta(Block blockIn, World worldIn, PlayerEntity player, BlockPos pos, BlockState state) {
        return StatHelper.getBlockStateStats(state);
    }
}
