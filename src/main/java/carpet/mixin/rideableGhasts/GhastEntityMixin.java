package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.class_3092;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GhastEntity.class)
public class GhastEntityMixin extends FlyingEntity {
    public GhastEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "initGoals", at = @At("RETURN"))
    private void addNewTasks(CallbackInfo ci) {
        this.goalSelector.add(3, new GhastHelper.AIFollowClues((GhastEntity) (Object) this));
        this.goalSelector.add(4, new GhastHelper.AIFindOwner((GhastEntity) (Object) this));
    }

    @Redirect(method = "initGoals", at = @At(value = "NEW", target = "net/minecraft/class_3092"))
    private class_3092 replaceTargetTask(MobEntity ghast) {
        return new GhastHelper.GhastEntityAIFindEntityNearestPlayer(ghast);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.random.nextInt(400) == 0 && CarpetSettings.rideableGhasts && this.deathTime == 0 && this.hasPassengers()) {
            this.heal(1.0F);
        }
    }

    @Override
    protected boolean interactMob(PlayerEntity player, Hand hand) {
        if (!(CarpetSettings.rideableGhasts) || this.hasPassengers()) {
            return super.interactMob(player, hand);
        }
        if (!GhastHelper.is_yo_bro((GhastEntity) (Object) this, player)) {
            return super.interactMob(player, hand);
        }
        boolean worked = super.interactMob(player, hand);
        if (!worked) {
            player.startRiding(this, true);
            player.getItemCooldownManager().set(Items.FIRE_CHARGE, 1);
        }
        return false;
    }

    @Override
    public Entity getPrimaryPassenger() {
        return this.getPassengerList().isEmpty() ? null : this.getPassengerList().get(0);
    }

    @Override
    public double getMountedHeightOffset() {
        if (CarpetSettings.rideableGhasts) return this.height - 0.2;
        return super.getMountedHeightOffset();
    }
}
