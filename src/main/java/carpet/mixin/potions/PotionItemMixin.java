package carpet.mixin.potions;

import carpet.utils.extensions.ExtendedStatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @Inject(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    private void preAddPotionEffect(ItemStack stack, World worldIn, LivingEntity entityLiving, CallbackInfoReturnable<ItemStack> cir) {
        ExtendedStatusEffectInstance.ItemPotionHolder.itemPotion = true;
    }

    @Inject(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)V", shift = At.Shift.AFTER))
    private void postAddPotionEffect(ItemStack stack, World worldIn, LivingEntity entityLiving, CallbackInfoReturnable<ItemStack> cir) {
        ExtendedStatusEffectInstance.ItemPotionHolder.itemPotion = false;
    }
}
