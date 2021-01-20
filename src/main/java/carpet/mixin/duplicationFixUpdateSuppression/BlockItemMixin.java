package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlaceableItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    BlockItem.class,
    PlaceableItem.class
})
public class BlockItemMixin {
    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private void vanillaShrink(ItemStack stack, int quantity) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) stack.decrement(quantity);
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean setBlockState(World world, BlockPos pos, BlockState newState, int flags, PlayerEntity player, World worldIn, BlockPos pos1, Hand hand) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) return world.setBlockState(pos, newState, flags);
        ItemStack stack = player.getStackInHand(hand);
        stack.decrement(1);
        if (world.setBlockState(pos, newState, flags)) return true;
        // set block failed
        stack.increment(1);
        return false;
    }
}
