package carpet.mixin.tileTickLimit;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.TreeSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.ScheduledTick;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow @Final private TreeSet<ScheduledTick> field_31703;

    @Inject(method = "method_26051", at = @At(value = "CONSTANT", args = "intValue=65536", ordinal = 1))
    private void logTileTickLimit(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        if (LoggerRegistry.__tileTickLimit) {
            int scheduled = field_31703.size();
            LoggerRegistry.getLogger("tileTickLimit").log(() -> new Text[] {
                Messenger.s(null, String.format("Reached tile tick limit (%d > %d)", scheduled, CarpetSettings.tileTickLimit))
            }, "NUMBER", scheduled, "LIMIT", CarpetSettings.tileTickLimit);
        }
    }

    @ModifyConstant(method = "method_26051", constant = @Constant(intValue = 65536))
    private int tileTickLimit(int origValue) {
        int limit = CarpetSettings.tileTickLimit;
        return limit < 0 ? Integer.MAX_VALUE : limit;
    }
}
