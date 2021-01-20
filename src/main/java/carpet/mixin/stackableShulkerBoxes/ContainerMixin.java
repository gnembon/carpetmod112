package carpet.mixin.stackableShulkerBoxes;

import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Container.class)
public class ContainerMixin {
    @Redirect(method = "onSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areTagsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 0))
    private boolean areTagsEqualAndStackable(ItemStack stackA, ItemStack stackB) {
        // Check If item can stack, Always true in vanilla CARPET-XCOM
        return ItemStack.areTagsEqual(stackA, stackB) && stackB.isStackable();
    }
}
