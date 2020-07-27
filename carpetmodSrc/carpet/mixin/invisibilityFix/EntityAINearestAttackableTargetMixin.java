package carpet.mixin.invisibilityFix;

import carpet.CarpetSettings;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityAINearestAttackableTarget.class)
public abstract class EntityAINearestAttackableTargetMixin<T extends EntityLivingBase> extends EntityAITarget {
    @Shadow @Final protected Predicate<? super T> targetEntitySelector;
    @Shadow protected T targetEntity;

    public EntityAINearestAttackableTargetMixin(EntityCreature creature, boolean checkSight) {
        super(creature, checkSight);
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "shouldExecute", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false))
    private <E> E replaceListGet(List<E> list, int index) {
        E first = list.get(index);
        if (!CarpetSettings.invisibilityFix || !(first instanceof EntityPlayer)) return first;
        return (E) this.taskOwner.world.getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double) this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(), player -> {
            ItemStack headSlot = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

            if (headSlot.getItem() == Items.SKULL) {
                int meta = headSlot.getItemDamage();
                boolean skeletonSkull = taskOwner instanceof EntitySkeleton && meta == 0;
                boolean zombieSkull = taskOwner instanceof EntityZombie && meta == 2;
                boolean creeperSkull = taskOwner instanceof EntityCreeper && meta == 4;

                if (skeletonSkull || zombieSkull || creeperSkull) {
                    return 0.5;
                }
            }

            return 1.0;
        }, (Predicate<EntityPlayer>) this.targetEntitySelector);
    }

    @Inject(method = "shouldExecute", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    private void returnFalseIfNull(CallbackInfoReturnable<Boolean> cir) {
        if (this.targetEntity == null) cir.setReturnValue(false);
    }
}
