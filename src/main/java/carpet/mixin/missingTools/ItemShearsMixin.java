package carpet.mixin.missingTools;

import carpet.CarpetSettings;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemShears.class)
public class ItemShearsMixin {

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void missingTools(ItemStack stack, IBlockState state, CallbackInfoReturnable<Float> cir) {
        if (!CarpetSettings.missingTools) return;
        if (state.getMaterial() == Material.SPONGE) cir.setReturnValue(15f);
    }
}
