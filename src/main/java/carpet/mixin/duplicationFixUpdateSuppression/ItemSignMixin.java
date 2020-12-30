package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemSign.class)
public class ItemSignMixin {
    @Inject(method = "onItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumFacing;UP:Lnet/minecraft/util/EnumFacing;", ordinal = 1))
    private void shrinkBefore(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir) {
        if (CarpetSettings.duplicationFixUpdateSuppression) player.getHeldItem(hand).shrink(1);
    }

    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V"))
    private void vanillaShrink(ItemStack itemStack, int quantity) {
        if (CarpetSettings.duplicationFixUpdateSuppression) return;
        itemStack.shrink(quantity);
    }
}
