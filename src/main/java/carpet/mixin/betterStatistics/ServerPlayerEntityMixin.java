package carpet.mixin.betterStatistics;

import carpet.helpers.StatSubItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow public abstract void increaseStat(Stat stat, int amount);

    @Inject(method = "increaseStat", at = @At("RETURN"))
    private void onAddStat(Stat stat, int amount, CallbackInfo ci) {
        if (stat instanceof StatSubItem) {
            increaseStat(((StatSubItem) stat).getBase(), amount);
        }
    }
}
