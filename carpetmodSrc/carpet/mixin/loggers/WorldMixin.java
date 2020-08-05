package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow protected WorldInfo worldInfo;
    @Shadow @Nullable public abstract MinecraftServer getMinecraftServer();

    @Inject(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V"), slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V", ordinal = 1),
        to = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V", ordinal = 2, shift = At.Shift.AFTER)
    ))
    private void onSetThunderTime(CallbackInfo ci) {
        // Log Weather CARPET-XCOM
        if (LoggerRegistry.__weather) {
            LoggerRegistry.getLogger("weather").log(()-> new ITextComponent[]{
                Messenger.s(null, "Thunder is set to: " + this.worldInfo.isThundering() + " time: " + this.worldInfo.getThunderTime() + " Server time: " + getMinecraftServer().getTickCounter())
            },
            "TYPE", "Thunder",
            "THUNDERING", this.worldInfo.isThundering(),
            "TIME", this.worldInfo.getThunderTime());
        }
    }


    @Inject(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V", ordinal = 1),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V", ordinal = 2, shift = At.Shift.AFTER)
    ))
    private void onSetRainTime(CallbackInfo ci) {
        // Log Weather CARPET-XCOM
        if (LoggerRegistry.__weather) {
            LoggerRegistry.getLogger("weather").log(() -> new ITextComponent[]{
                Messenger.s(null, "Rain is set to: " + this.worldInfo.isRaining() + " time: " + this.worldInfo.getRainTime() + " Server time: " + getMinecraftServer().getTickCounter())
            },
            "TYPE", "Rain",
            "RAINING", this.worldInfo.isRaining(),
            "TIME", this.worldInfo.getRainTime());
        }
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void invisDebugAtRemoveEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityPlayerMP) DebugLogHelper.invisDebug(() -> "r1: " + entity);
    }

    @Inject(method = "removeEntityDangerously", at = @At("HEAD"))
    private void invisDebugAtRemoveEntityDangerously(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityPlayerMP) DebugLogHelper.invisDebug(() -> "r1: " + entity);
    }
}
