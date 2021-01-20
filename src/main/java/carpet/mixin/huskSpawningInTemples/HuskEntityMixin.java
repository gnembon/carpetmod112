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
    @Redirect(method = "canMobSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isSkyVisible(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean alwaysInTemple(World world, BlockPos pos) {
        if (world.isSkyVisible(pos)) return true;
        if (!CarpetSettings.huskSpawningInTemples) return false;
        return ((ServerWorld) world).getChunkManager().method_33446(world, "Temple", pos);
    }
}
