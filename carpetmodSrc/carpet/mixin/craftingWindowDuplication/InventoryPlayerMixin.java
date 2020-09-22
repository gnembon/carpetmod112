package carpet.mixin.craftingWindowDuplication;

import carpet.utils.extensions.DupingPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InventoryPlayer.class)
public class InventoryPlayerMixin {
    @Shadow public EntityPlayer player;

    @Inject(method = "storePartialItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getFirstEmptyStack()I", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void dupeItemSlotStorePartialItemStack(ItemStack itemStackIn, CallbackInfoReturnable<Integer> cir, int slot) {
        ((DupingPlayer) player).dupeItem(slot);
    }

    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getFirstEmptyStack()I"))
    private void dupeItemSlotAdd(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ((DupingPlayer) player).dupeItem(slot);
    }
}
