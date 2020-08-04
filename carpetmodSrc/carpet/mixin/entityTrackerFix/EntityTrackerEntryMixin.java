package carpet.mixin.entityTrackerFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private int range;
    @Shadow @Final private Entity trackedEntity;

    @Redirect(method = "isVisibleTo", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityTrackerEntry;range:I"))
    private int entityTrackerFix(EntityTrackerEntry entry) {
        if (!CarpetSettings.entityTrackerFix) return range;
        if (trackedEntity instanceof EntityMinecart || trackedEntity instanceof EntityBoat) {
            for (Entity e : trackedEntity.getPassengers()) {
                if (e instanceof EntityPlayer) return 512;
            }
        }
        return range;
    }
}
