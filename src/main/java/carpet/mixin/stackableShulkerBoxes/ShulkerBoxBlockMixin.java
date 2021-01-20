package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetSettings;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
    @Redirect(method = "onBlockRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void avoidEmptyTag(ItemStack itemStack, CompoundTag nbt) {
        if (CarpetSettings.stackableEmptyShulkerBoxes && nbt.isEmpty()) return;
        itemStack.setTag(nbt);
    }
}
