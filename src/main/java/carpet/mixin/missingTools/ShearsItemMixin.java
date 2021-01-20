package carpet.mixin.missingTools;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

    @Inject(method = "getMiningSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void missingTools(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        if (!CarpetSettings.missingTools) return;
        if (state.getMaterial() == Material.SPONGE) cir.setReturnValue(15f);
    }
}
