package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import carpet.helpers.CarefulBreakHelper;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
    @Shadow public World field_31754;
    @Shadow public ServerPlayerEntity player;

    @Shadow protected abstract boolean method_33542(BlockPos pos);

    @Redirect(method = "method_33540", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;method_33542(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean dupeFixEmulateRemove(ServerPlayerInteractionManager playerInteractionManager, BlockPos pos) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) {
            try {
                CarefulBreakHelper.miningPlayer = player;
                return this.method_33542(pos);
            } finally {
                CarefulBreakHelper.miningPlayer = null;
            }
        }
        int y = pos.getY();
        if (y >= 0 && y < 256) {
            if (field_31754.isClient || field_31754.getLevelProperties().getGeneratorType() != LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
                return field_31754.getBlockState(pos) != Blocks.AIR.getDefaultState();
            }
        }
        return false;
    }

    @Inject(method = "method_33540", at = @At("TAIL"))
    private void dupeFixRemoveBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Update suppression duplication fix removing the block post inventory updates with exception to flowing water as ice can turn into flowing water CARPET-XCOM
        if (!CarpetSettings.duplicationFixUpdateSuppression || field_31754.getBlockState(pos) == Blocks.FLOWING_WATER.getDefaultState()) {
            return;
        }
        try {
            CarefulBreakHelper.miningPlayer = player;
            this.method_33542(pos);
        } finally {
            CarefulBreakHelper.miningPlayer = null;
        }
    }
}
