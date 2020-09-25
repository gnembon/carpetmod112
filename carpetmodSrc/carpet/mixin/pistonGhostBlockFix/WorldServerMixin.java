package carpet.mixin.pistonGhostBlockFix;

import carpet.utils.extensions.ExtendedWorldServerPistonGhostBlockFix;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public class WorldServerMixin implements ExtendedWorldServerPistonGhostBlockFix {
    private boolean blockActionsProcessed;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        blockActionsProcessed = false;
    }

    @Inject(method = "sendQueuedBlockEvents", at = @At("RETURN"))
    private void onBlockEventsEnd(CallbackInfo ci) {
        blockActionsProcessed = true;
    }

    @Override
    public boolean haveBlockActionsProcessed() {
        return blockActionsProcessed;
    }
}
