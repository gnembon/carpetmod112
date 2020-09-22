package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import carpet.helpers.CarefulBreakHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
    @Shadow public World world;
    @Shadow public EntityPlayerMP player;

    @Shadow protected abstract boolean removeBlock(BlockPos pos);

    @Redirect(method = "tryHarvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;removeBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean dupeFixEmulateRemove(PlayerInteractionManager playerInteractionManager, BlockPos pos) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) {
            try {
                CarefulBreakHelper.miningPlayer = player;
                return this.removeBlock(pos);
            } finally {
                CarefulBreakHelper.miningPlayer = null;
            }
        }
        int y = pos.getY();
        if (y >= 0 && y < 256) {
            if (world.isRemote || world.getWorldInfo().getTerrainType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                return world.getBlockState(pos) != Blocks.AIR.getDefaultState();
            }
        }
        return false;
    }

    @Inject(method = "tryHarvestBlock", at = @At("TAIL"))
    private void dupeFixRemoveBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Update suppression duplication fix removing the block post inventory updates with exception to flowing water as ice can turn into flowing water CARPET-XCOM
        if (!CarpetSettings.duplicationFixUpdateSuppression || world.getBlockState(pos) == Blocks.FLOWING_WATER.getDefaultState()) {
            return;
        }
        try {
            CarefulBreakHelper.miningPlayer = player;
            this.removeBlock(pos);
        } finally {
            CarefulBreakHelper.miningPlayer = null;
        }
    }
}
