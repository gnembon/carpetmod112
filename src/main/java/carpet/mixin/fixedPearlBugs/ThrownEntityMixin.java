package carpet.mixin.fixedPearlBugs;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.thrown.ThrownEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin extends Entity {
    @Shadow private String ownerName;
    @Shadow protected LivingEntity owner;

    public ThrownEntityMixin(World worldIn) {
        super(worldIn);
    }

    // Fixes pearls disappearing when players relog similar to 1.15 CARPET-XCOM
    @Inject(method = "getOwner", at = @At("HEAD"))
    private void pearlCheck(CallbackInfoReturnable<LivingEntity> cir) {
        if (!CarpetSettings.fixedPearlBugs) return;
        if (ownerName == null) {
            if (owner == null) return;
            ownerName = owner.getName();
        }
        try {
            Entity e = ((ServerWorld) world).getEntity(UUID.fromString(ownerName));
            if (!world.field_23576.contains(e)) {
                owner = null;
            }
        } catch (Exception e) {
            owner = null;
        }
    }
}
