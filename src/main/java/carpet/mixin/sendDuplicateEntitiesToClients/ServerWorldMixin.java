package carpet.mixin.sendDuplicateEntitiesToClients;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow @Final private EntityTracker wanderingTraderManager;

    @Inject(method = "method_33481", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER, remap = false))
    private void sendDuplicateEntitiesToClients(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.sendDuplicateEntitiesToClients) {
            wanderingTraderManager.method_33431(entity);
        }
    }
}
