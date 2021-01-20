package carpet.mixin.ai;

import carpet.helpers.AIHelper;
import net.minecraft.class_6483;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_6483.class)
public abstract class EntityAIVillagerMateMixin extends Goal {
    @Shadow @Final private VillagerEntity field_33432;

    @Shadow private int field_33435;

    @Inject(method = "canStart", at = @At("HEAD"))
    private void readyToMate(CallbackInfoReturnable<Boolean> cir) {
        if (this.field_33432.getBreedingAge() < 5 && this.field_33432.getBreedingAge() > 0) {
            AIHelper.setDetailedInfo(this.field_33432, this, "Ready to Mate");
        }
    }

    @Inject(method = "canStart", at = @At(value = "RETURN", ordinal = 0))
    private void waiting(CallbackInfoReturnable<Boolean> cir) {
        int growingAge = this.field_33432.getBreedingAge();
        if (growingAge >= 5) {
            AIHelper.setDetailedInfo(this.field_33432, this, () -> "Waiting: " + growingAge);
        }
    }

    @Inject(method = "canStart", at = @At(value = "RETURN", ordinal = 2))
    private void outsideOfVillage(CallbackInfoReturnable<Boolean> cir) {
        AIHelper.setDetailedInfo(this.field_33432, this, "Outside of a village");
    }

    @Inject(method = "canStart", at = @At(value = "RETURN", ordinal = 5))
    private void dontWantToMate(CallbackInfoReturnable<Boolean> cir) {
        AIHelper.setDetailedInfo(this.field_33432, this, "Don't want to mate");
    }

    @Inject(method = "start", at = @At(value = "RETURN"))
    private void inLove300(CallbackInfo ci) {
        AIHelper.setDetailedInfo(this.field_33432, this, "In love: 300");
    }

    @Inject(method = "stop", at = @At(value = "RETURN"))
    private void onResetTask(CallbackInfo ci) {
        AIHelper.setDetailedInfo(this.field_33432, this, "Ready to Mate");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/control/LookControl;lookAt(Lnet/minecraft/entity/Entity;FF)V"))
    private void onUpdateTask(CallbackInfo ci) {
        int matingTimeout = this.field_33435;
        if (matingTimeout > 0) {
            AIHelper.setDetailedInfo(this.field_33432, this, () -> "In love: " + matingTimeout);
        } else {
            AIHelper.setDetailedInfo(this.field_33432, this, "Ready to Mate");
        }
    }
}
