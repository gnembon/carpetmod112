package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TrajectoryLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.thrown.ThrownEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin extends Entity {
    private TrajectoryLogHelper logHelper;

    public ThrownEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void onInit(World worldIn, CallbackInfo ci) {
        if (LoggerRegistry.__projectiles) {
            logHelper = new TrajectoryLogHelper("projectiles");
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/thrown/ThrownEntity;updatePosition(DDD)V"))
    private void onTick(CallbackInfo ci) {
        if (LoggerRegistry.__projectiles && logHelper != null) {
            logHelper.onTick(x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (LoggerRegistry.__projectiles && logHelper != null) {
            logHelper.onFinish();
        }
    }
}
