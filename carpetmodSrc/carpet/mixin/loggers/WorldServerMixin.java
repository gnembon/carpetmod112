package carpet.mixin.loggers;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import carpet.utils.extensions.ExtendedWorld;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    @Shadow @Final private MinecraftServer server;

    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;tickUpdates(Z)Z", shift = At.Shift.AFTER))
    private void rngTickUpdates(CallbackInfo ci) {
        logAndSetRng("TickUp.");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;sendQueuedBlockEvents()V", shift = At.Shift.AFTER))
    private void rngBlockEvents(CallbackInfo ci) {
        logAndSetRng("BlockEv.");
    }


    @Unique private void logAndSetRng(String phase) {
        if (LoggerRegistry.__rng) {
            LoggerRegistry.getLogger("rng").log(() -> new ITextComponent[]{
                    Messenger.s(null, String.format("RNG %s t:%d seed:%d d:%s", phase, server.getTickCounter(), ((ExtendedWorld) this).getRandSeed(), provider.getDimensionType().name()))
            });
        }
        if (CarpetSettings.setSeed != 0) {
            this.rand.setSeed(CarpetSettings.setSeed ^ 0x5DEECE66DL);
        }
    }
}
