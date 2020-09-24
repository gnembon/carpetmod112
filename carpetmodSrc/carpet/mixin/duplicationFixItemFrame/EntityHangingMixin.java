package carpet.mixin.duplicationFixItemFrame;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.MoverType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityHanging.class)
public abstract class EntityHangingMixin extends Entity {
    public EntityHangingMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = {
        "onUpdate",
        "attackEntityFrom",
        "move",
        "addVelocity"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;setDead()V"))
    private void moveSetDead1(EntityHanging entity) {}

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;onBroken(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void moveSetDead2(CallbackInfo ci) {
        setDead();
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;onBroken(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void moveSetDead2(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        setDead();
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;onBroken(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void moveSetDead2(MoverType type, double x, double y, double z, CallbackInfo ci) {
        setDead();
    }

    @Inject(method = "addVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;onBroken(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void moveSetDead2(double x, double y, double z, CallbackInfo ci) {
        setDead();
    }
}
