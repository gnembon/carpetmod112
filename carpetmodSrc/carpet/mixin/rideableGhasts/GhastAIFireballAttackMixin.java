package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.monster.EntityGhast$AIFireballAttack")
public abstract class GhastAIFireballAttackMixin extends EntityAIBase {
    @Shadow @Final private EntityGhast parentEntity;

    @Override
    @Overwrite
    public boolean shouldExecute() {
        if (this.parentEntity.getAttackTarget() == null) {
            return false;
        }
        if (CarpetSettings.rideableGhasts && this.parentEntity.hasCustomName()) {
            if (this.parentEntity.isBeingRidden()) {
                //reset the attack;
                this.resetTask();
                return false;
            }
            Entity at = this.parentEntity.getAttackTarget();
            if (at instanceof EntityPlayer && GhastHelper.is_yo_bro(this.parentEntity, (EntityPlayer) at)) {
                //reset the attack
                this.resetTask();
                return false;
            }
        }
        return true;
    }

    @Inject(method = "resetTask()V", at = @At("RETURN"))
    private void onReset(CallbackInfo ci) {
        if (CarpetSettings.rideableGhasts) {
            this.parentEntity.setAttackTarget(null);
        }
    }
}
