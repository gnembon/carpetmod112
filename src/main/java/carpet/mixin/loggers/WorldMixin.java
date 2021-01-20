package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow protected LevelProperties properties;
    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Inject(method = "method_26148", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_28266(I)V"), slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_28266(I)V", ordinal = 1),
        to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_28266(I)V", ordinal = 2, shift = At.Shift.AFTER)
    ))
    private void onSetThunderTime(CallbackInfo ci) {
        // Log Weather CARPET-XCOM
        if (LoggerRegistry.__weather) {
            LoggerRegistry.getLogger("weather").log(()-> new Text[]{
                Messenger.s(null, "Thunder is set to: " + this.properties.method_28285() + " time: " + this.properties.method_28287() + " Server time: " + getServer().getTicks())
            },
            "TYPE", "Thunder",
            "THUNDERING", this.properties.method_28285(),
            "TIME", this.properties.method_28287());
        }
    }


    @Inject(method = "method_26148", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_28270(I)V"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_28270(I)V", ordinal = 1),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_28270(I)V", ordinal = 2, shift = At.Shift.AFTER)
    ))
    private void onSetRainTime(CallbackInfo ci) {
        // Log Weather CARPET-XCOM
        if (LoggerRegistry.__weather) {
            LoggerRegistry.getLogger("weather").log(() -> new Text[]{
                Messenger.s(null, "Rain is set to: " + this.properties.method_28289() + " time: " + this.properties.method_28291() + " Server time: " + getServer().getTicks())
            },
            "TYPE", "Rain",
            "RAINING", this.properties.method_28289(),
            "TIME", this.properties.method_28291());
        }
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void invisDebugAtRemoveEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity) DebugLogHelper.invisDebug(() -> "r1: " + entity);
    }

    @Inject(method = "method_26123", at = @At("HEAD"))
    private void invisDebugAtRemoveEntityDangerously(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity) DebugLogHelper.invisDebug(() -> "r1: " + entity);
    }
}
