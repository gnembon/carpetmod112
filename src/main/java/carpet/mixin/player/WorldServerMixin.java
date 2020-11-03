package carpet.mixin.player;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Inject(method = "canAddEntity", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BY, by = 2, remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void loginMinecartFix(Entity entityIn, CallbackInfoReturnable<Boolean> cir, UUID uuid, Entity entity) {
        if (EntityPlayerMPFake.shouldFixMinecart()) {
            entity.isDead = true;
            cir.setReturnValue(true);
        }
    }
}
