package carpet.mixin.ai;

import carpet.helpers.AIHelper;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityAIVillagerMate.class)
public abstract class EntityAIVillagerMateMixin extends EntityAIBase {
    @Shadow @Final private EntityVillager villager;

    @Shadow private int matingTimeout;

    @Inject(method = "shouldExecute", at = @At("HEAD"))
    private void readyToMate(CallbackInfoReturnable<Boolean> cir) {
        if (this.villager.getGrowingAge() < 5 && this.villager.getGrowingAge() > 0) {
            AIHelper.setDetailedInfo(this.villager, this, "Ready to Mate");
        }
    }

    @Inject(method = "shouldExecute", at = @At(value = "RETURN", ordinal = 0))
    private void waiting(CallbackInfoReturnable<Boolean> cir) {
        int growingAge = this.villager.getGrowingAge();
        if (growingAge >= 5) {
            AIHelper.setDetailedInfo(this.villager, this, () -> "Waiting: " + growingAge);
        }
    }

    @Inject(method = "shouldExecute", at = @At(value = "RETURN", ordinal = 2))
    private void outsideOfVillage(CallbackInfoReturnable<Boolean> cir) {
        AIHelper.setDetailedInfo(this.villager, this, "Outside of a village");
    }

    @Inject(method = "shouldExecute", at = @At(value = "RETURN", ordinal = 5))
    private void dontWantToMate(CallbackInfoReturnable<Boolean> cir) {
        AIHelper.setDetailedInfo(this.villager, this, "Don't want to mate");
    }

    @Inject(method = "startExecuting", at = @At(value = "RETURN"))
    private void inLove300(CallbackInfo ci) {
        AIHelper.setDetailedInfo(this.villager, this, "In love: 300");
    }

    @Inject(method = "resetTask", at = @At(value = "RETURN"))
    private void onResetTask(CallbackInfo ci) {
        AIHelper.setDetailedInfo(this.villager, this, "Ready to Mate");
    }

    @Inject(method = "updateTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/EntityLookHelper;setLookPositionWithEntity(Lnet/minecraft/entity/Entity;FF)V"))
    private void onUpdateTask(CallbackInfo ci) {
        int matingTimeout = this.matingTimeout;
        if (matingTimeout > 0) {
            AIHelper.setDetailedInfo(this.villager, this, () -> "In love: " + matingTimeout);
        } else {
            AIHelper.setDetailedInfo(this.villager, this, "Ready to Mate");
        }
    }
}
