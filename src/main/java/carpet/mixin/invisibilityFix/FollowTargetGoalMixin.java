package carpet.mixin.invisibilityFix;

import carpet.CarpetSettings;
import com.google.common.base.Predicate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FollowTargetGoal.class)
public abstract class FollowTargetGoalMixin<T extends LivingEntity> extends TrackTargetGoal {
    @Shadow @Final protected Predicate<? super T> field_33585;
    @Shadow protected T targetEntity;

    public FollowTargetGoalMixin(PathAwareEntity creature, boolean checkSight) {
        super(creature, checkSight);
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false))
    private <E> E replaceListGet(List<E> list, int index) {
        E first = list.get(index);
        if (!CarpetSettings.invisibilityFix || !(first instanceof PlayerEntity)) return first;
        return (E) this.mob.world.getClosestPlayer(this.mob.x, this.mob.y + (double) this.mob.getStandingEyeHeight(), this.mob.z, this.getFollowRange(), this.getFollowRange(), player -> {
            ItemStack headSlot = player.getEquippedStack(EquipmentSlot.HEAD);

            if (headSlot.getItem() == Items.SKULL) {
                int meta = headSlot.getDamage();
                boolean skeletonSkull = mob instanceof SkeletonEntity && meta == 0;
                boolean zombieSkull = mob instanceof ZombieEntity && meta == 2;
                boolean creeperSkull = mob instanceof CreeperEntity && meta == 4;

                if (skeletonSkull || zombieSkull || creeperSkull) {
                    return 0.5;
                }
            }

            return 1.0;
        }, (Predicate<PlayerEntity>) this.field_33585);
    }

    @Inject(method = "canStart", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    private void returnFalseIfNull(CallbackInfoReturnable<Boolean> cir) {
        if (this.targetEntity == null) cir.setReturnValue(false);
    }
}
