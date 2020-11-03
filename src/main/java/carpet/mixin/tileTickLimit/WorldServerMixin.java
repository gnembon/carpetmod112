package carpet.mixin.tileTickLimit;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.TreeSet;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;

    @Inject(method = "tickUpdates", at = @At(value = "CONSTANT", args = "intValue=65536", ordinal = 1))
    private void logTileTickLimit(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        if (LoggerRegistry.__tileTickLimit) {
            int scheduled = pendingTickListEntriesTreeSet.size();
            LoggerRegistry.getLogger("tileTickLimit").log(() -> new ITextComponent[] {
                Messenger.s(null, String.format("Reached tile tick limit (%d > %d)", scheduled, CarpetSettings.tileTickLimit))
            }, "NUMBER", scheduled, "LIMIT", CarpetSettings.tileTickLimit);
        }
    }

    @ModifyConstant(method = "tickUpdates", constant = @Constant(intValue = 65536))
    private int tileTickLimit(int origValue) {
        int limit = CarpetSettings.tileTickLimit;
        return limit < 0 ? Integer.MAX_VALUE : limit;
    }
}
