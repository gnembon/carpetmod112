package carpet.mixin.invisibilityFix;

import carpet.CarpetSettings;
import com.google.common.base.Predicate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntityWithAi;
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
    @Shadow protected T field_33586;

    public FollowTargetGoalMixin(MobEntityWithAi creature, boolean checkSight) {
        super(creature, checkSight);
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false))
    private <E> E replaceListGet(List<E> list, int index) {
        E first = list.get(index);
        if (!CarpetSettings.invisibilityFix || !(first instanceof PlayerEntity)) return first;
        return (E) this.field_33608.world.method_25968(this.field_33608.field_33071, this.field_33608.field_33072 + (double) this.field_33608.method_34518(), this.field_33608.field_33073, this.getFollowRange(), this.getFollowRange(), player -> {
            ItemStack headSlot = player.getEquippedStack(EquipmentSlot.HEAD);

            if (headSlot.getItem() == Items.SKULL) {
                int meta = headSlot.getDamage();
                boolean skeletonSkull = field_33608 instanceof SkeletonEntity && meta == 0;
                boolean zombieSkull = field_33608 instanceof ZombieEntity && meta == 2;
                boolean creeperSkull = field_33608 instanceof CreeperEntity && meta == 4;

                if (skeletonSkull || zombieSkull || creeperSkull) {
                    return 0.5;
                }
            }

            return 1.0;
        }, (Predicate<PlayerEntity>) this.field_33585);
    }

    @Inject(method = "canStart", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    private void returnFalseIfNull(CallbackInfoReturnable<Boolean> cir) {
        if (this.field_33586 == null) cir.setReturnValue(false);
    }
}
