package carpet.mixin.instantDrinkPotion;

import carpet.CarpetSettings;
import net.minecraft.item.ItemPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemPotion.class)
public class ItemPotionMixin {
    @ModifyConstant(method = "getMaxItemUseDuration", constant = @Constant(intValue = 32))
    private int instantDrink(int orig) {
        if (CarpetSettings.instantDrinkPotion) return 1;
        return orig;
    }
}
