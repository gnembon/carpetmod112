package carpet.mixin.leashFix;

import carpet.utils.extensions.EntityWithPostLoad;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "loadEntityUnchecked", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;method_34496()[Lnet/minecraft/entity/Entity;"))
    private void applyLeashFix(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityWithPostLoad) ((EntityWithPostLoad) entity).postLoad();
    }
}
