package carpet.mixin.fixedPearlBugs;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(EntityThrowable.class)
public abstract class EntityThrowableMixin extends Entity {
    @Shadow private String throwerName;
    @Shadow protected EntityLivingBase thrower;

    public EntityThrowableMixin(World worldIn) {
        super(worldIn);
    }

    // Fixes pearls disappearing when players relog similar to 1.15 CARPET-XCOM
    @Inject(method = "getThrower", at = @At("HEAD"))
    private void pearlCheck(CallbackInfoReturnable<EntityLivingBase> cir) {
        if (!CarpetSettings.fixedPearlBugs) return;
        if (throwerName == null) {
            if (thrower == null) return;
            throwerName = thrower.getName();
        }
        try {
            Entity e = ((WorldServer) world).getEntityFromUuid(UUID.fromString(throwerName));
            if (!world.playerEntities.contains(e)) {
                thrower = null;
            }
        } catch (Exception e) {
            thrower = null;
        }
    }
}
