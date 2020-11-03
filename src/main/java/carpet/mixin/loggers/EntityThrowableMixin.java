package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TrajectoryLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityThrowable.class)
public abstract class EntityThrowableMixin extends Entity {
    private TrajectoryLogHelper logHelper;

    public EntityThrowableMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void onInit(World worldIn, CallbackInfo ci) {
        if (LoggerRegistry.__projectiles) {
            logHelper = new TrajectoryLogHelper("projectiles");
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/EntityThrowable;setPosition(DDD)V"))
    private void onTick(CallbackInfo ci) {
        if (LoggerRegistry.__projectiles && logHelper != null) {
            logHelper.onTick(posX, posY, posZ, motionX, motionY, motionZ);
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        if (LoggerRegistry.__projectiles && logHelper != null) {
            logHelper.onFinish();
        }
    }
}
