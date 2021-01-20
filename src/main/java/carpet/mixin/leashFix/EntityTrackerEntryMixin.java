package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "method_33553", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onStartedTracking(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void leashFix(ServerPlayerEntity playerMP, CallbackInfo ci) {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.off || !(entity instanceof MobEntity)) return;
        playerMP.networkHandler.sendPacket(new EntityAttachS2CPacket(entity, ((MobEntity) entity).getHoldingEntity()));
    }
}
