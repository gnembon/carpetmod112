package carpet.mixin.unloadedEntityFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "method_26050", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleportRequested()Z"))
    private boolean unloadedEntityFix(Entity entity) {
        // Faster entitys can move into unloaded chunks and can get stuck in memory lagging the server. this fixes it CARPET-XCOM
        return entity.teleportRequested() || CarpetSettings.unloadedEntityFix;
    }
}
