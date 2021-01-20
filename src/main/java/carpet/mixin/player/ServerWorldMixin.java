package carpet.mixin.player;

import carpet.patches.FakeServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "method_33481", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BY, by = 2, remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void loginMinecartFix(Entity entityIn, CallbackInfoReturnable<Boolean> cir, UUID uuid, Entity entity) {
        if (FakeServerPlayerEntity.shouldFixMinecart()) {
            entity.removed = true;
            cir.setReturnValue(true);
        }
    }
}
