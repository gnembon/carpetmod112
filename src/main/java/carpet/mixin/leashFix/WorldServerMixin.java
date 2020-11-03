package carpet.mixin.leashFix;

import carpet.utils.extensions.EntityWithPostLoad;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Inject(method = "onEntityAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getParts()[Lnet/minecraft/entity/Entity;"))
    private void applyLeashFix(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityWithPostLoad) ((EntityWithPostLoad) entity).postLoad();
    }
}
