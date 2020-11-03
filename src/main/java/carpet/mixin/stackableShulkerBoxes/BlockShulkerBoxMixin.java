package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetSettings;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockShulkerBox.class)
public class BlockShulkerBoxMixin {
    @Redirect(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    private void avoidEmptyTag(ItemStack itemStack, NBTTagCompound nbt) {
        if (CarpetSettings.stackableEmptyShulkerBoxes && nbt.isEmpty()) return;
        itemStack.setTagCompound(nbt);
    }
}
