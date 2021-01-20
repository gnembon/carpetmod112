package carpet.mixin.maxEntityCollisions;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(method = "tickCramming", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false))
    private int maxEntityCollisions(List<Entity> list) {
        return CarpetSettings.maxEntityCollisions > 0 ? Math.min(list.size(), CarpetSettings.maxEntityCollisions) : list.size();
    }
}
