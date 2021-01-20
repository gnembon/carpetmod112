package carpet.mixin.persistentParrots;

import carpet.CarpetSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public PlayerAbilities abilities;
    @Shadow protected abstract void method_25049();
    @Shadow protected abstract void method_25066(CompoundTag tag);
    @Shadow protected abstract void setShoulderEntityLeft(CompoundTag tag);
    @Shadow public abstract CompoundTag getShoulderEntityLeft();
    @Shadow public abstract CompoundTag getShoulderEntityRight();
    @Shadow protected abstract void setShoulderEntityRight(CompoundTag tag);

    public PlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z", ordinal = 1))
    private boolean isClientCheck(World world) {
        return false;
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onLivingUpdateEnd(CallbackInfo ci) {
        boolean parrots_will_drop = !CarpetSettings.persistentParrots || this.abilities.invulnerable;
        if (!this.world.isClient && ((parrots_will_drop && this.fallDistance > 0.5F) || this.isTouchingWater() || (parrots_will_drop && this.hasVehicle())) || this.abilities.flying) {
            this.method_25049();
        }
    }

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;method_25049()V"))
    private void dropParrotsOnAttack(PlayerEntity entityPlayer, DamageSource source, float amount) {
        if (CarpetSettings.persistentParrots && !this.isSneaking()) {
            if (this.random.nextFloat() < amount / 15.0) {
                this.method_25066(this.getShoulderEntityLeft());
                this.setShoulderEntityLeft(new CompoundTag());
            }
            if (this.random.nextFloat() < amount / 15.0) {
                this.method_25066(this.getShoulderEntityRight());
                this.setShoulderEntityRight(new CompoundTag());
            }
        }
    }
}
