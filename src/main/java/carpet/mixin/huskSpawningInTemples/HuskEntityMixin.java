package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HuskEntity.class)
public class HuskEntityMixin {
    @Redirect(method = "method_34765", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26129(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean alwaysInTemple(World world, BlockPos pos) {
        if (world.method_26129(pos)) return true;
        if (!CarpetSettings.huskSpawningInTemples) return false;
        return ((ServerWorld) world).getChunkManager().method_33446(world, "Temple", pos);
    }
}
