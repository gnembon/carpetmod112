package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityHusk.class)
public class EntityHuskMixin {
    @Redirect(method = "getCanSpawnHere", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;canSeeSky(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean alwaysInTemple(World world, BlockPos pos) {
        if (world.canSeeSky(pos)) return true;
        if (!CarpetSettings.huskSpawningInTemples) return false;
        return ((WorldServer)world).getChunkProvider().isInsideStructure(world, "Temple", pos);
    }
}
