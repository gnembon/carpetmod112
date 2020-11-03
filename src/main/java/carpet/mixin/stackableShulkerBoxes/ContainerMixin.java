package carpet.mixin.stackableShulkerBoxes;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Container.class)
public class ContainerMixin {
    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areItemStackTagsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 0))
    private boolean areTagsEqualAndStackable(ItemStack stackA, ItemStack stackB) {
        // Check If item can stack, Always true in vanilla CARPET-XCOM
        return ItemStack.areItemStackTagsEqual(stackA, stackB) && stackB.isStackable();
    }
}
