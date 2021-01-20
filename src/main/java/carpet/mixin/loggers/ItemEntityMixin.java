package carpet.mixin.loggers;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ItemLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();

    private ItemLogHelper logHelper;

    public ItemEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDD)V", at = @At("RETURN"))
    private void onInit(World world, double x, double y, double z, CallbackInfo ci) {
        if (LoggerRegistry.__items) {
            logHelper = new ItemLogHelper("items");
        }
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ItemEntity;age:I", ordinal = 2))
    private void onTick(CallbackInfo ci) {
        if (LoggerRegistry.__items && logHelper != null) {
            logHelper.onTick(x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;remove()V"))
    private void onFinish(CallbackInfo ci) {
        if (LoggerRegistry.__items && logHelper != null) {
            logHelper.onFinish("Despawn Timer");
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;remove()V"))
    private void onBroken(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.cactusCounter && source == DamageSource.CACTUS) {
            HopperCounter.cactus.add(world.getServer(), getStack());
        }

        if (LoggerRegistry.__items && logHelper != null) {
            logHelper.onFinish(source.getName());
        }
    }
}
