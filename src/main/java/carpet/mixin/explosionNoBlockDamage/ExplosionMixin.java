package carpet.mixin.explosionNoBlockDamage;

import carpet.CarpetSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Redirect(method = "method_25898", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", remap = false))
    private boolean addAll(List<BlockPos> list, Collection<? extends BlockPos> blocks) {
        return CarpetSettings.explosionNoBlockDamage || list.addAll(blocks);
    }
}
