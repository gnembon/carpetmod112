package carpet.mixin.loggers;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ItemLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin extends Entity {
    @Shadow public abstract ItemStack getItem();

    private ItemLogHelper logHelper;

    public EntityItemMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDD)V", at = @At("RETURN"))
    private void onInit(World world, double x, double y, double z, CallbackInfo ci) {
        if (LoggerRegistry.__items) {
            logHelper = new ItemLogHelper("items");
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityItem;age:I", ordinal = 2))
    private void onTick(CallbackInfo ci) {
        if (LoggerRegistry.__items && logHelper != null) {
            logHelper.onTick(posX, posY, posZ, motionX, motionY, motionZ);
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDead()V"))
    private void onFinish(CallbackInfo ci) {
        if (LoggerRegistry.__items && logHelper != null) {
            logHelper.onFinish("Despawn Timer");
        }
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDead()V"))
    private void onBroken(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.cactusCounter && source == DamageSource.CACTUS) {
            HopperCounter.cactus.add(world.getMinecraftServer(), getItem());
        }

        if (LoggerRegistry.__items && logHelper != null) {
            logHelper.onFinish(source.getDamageType());
        }
    }
}
