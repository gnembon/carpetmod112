package carpet.mixin.entityTrackerFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private int tickInterval;
    @Shadow @Final private Entity entity;

    @Redirect(method = "method_33555", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/EntityTrackerEntry;tickInterval:I"))
    private int entityTrackerFix(EntityTrackerEntry entry) {
        if (!CarpetSettings.entityTrackerFix) return tickInterval;
        if (entity instanceof AbstractMinecartEntity || entity instanceof BoatEntity) {
            for (Entity e : entity.getPassengerList()) {
                if (e instanceof PlayerEntity) return 512;
            }
        }
        return tickInterval;
    }
}
