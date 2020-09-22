package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    ItemBlock.class,
    ItemBlockSpecial.class
})
public class ItemBlockMixin {
    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V"))
    private void vanillaShrink(ItemStack stack, int quantity) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) stack.shrink(quantity);
    }

    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private boolean setBlockState(World world, BlockPos pos, IBlockState newState, int flags, EntityPlayer player, World worldIn, BlockPos pos1, EnumHand hand) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) return world.setBlockState(pos, newState, flags);
        ItemStack stack = player.getHeldItem(hand);
        stack.shrink(1);
        if (world.setBlockState(pos, newState, flags)) return true;
        // set block failed
        stack.grow(1);
        return false;
    }
}
