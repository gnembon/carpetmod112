package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityAttach;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity trackedEntity;

    @Inject(method = "updatePlayerEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;addEntity(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void leashFix(EntityPlayerMP playerMP, CallbackInfo ci) {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.off || !(trackedEntity instanceof EntityLiving)) return;
        playerMP.connection.sendPacket(new SPacketEntityAttach(trackedEntity, ((EntityLiving)trackedEntity).getLeashHolder()));
    }
}
