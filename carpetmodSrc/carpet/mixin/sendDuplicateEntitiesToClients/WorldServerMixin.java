package carpet.mixin.sendDuplicateEntitiesToClients;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Shadow @Final private EntityTracker entityTracker;

    @Inject(method = "canAddEntity", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER, remap = false))
    private void sendDuplicateEntitiesToClients(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.sendDuplicateEntitiesToClients) {
            entityTracker.track(entity);
        }
    }
}
