package carpet.mixin.betterStatistics;

import carpet.helpers.StatSubItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin {
    @Shadow public abstract void addStat(StatBase stat, int amount);

    @Inject(method = "addStat", at = @At("RETURN"))
    private void onAddStat(StatBase stat, int amount, CallbackInfo ci) {
        if (stat instanceof StatSubItem) {
            addStat(((StatSubItem) stat).getBase(), amount);
        }
    }
}
