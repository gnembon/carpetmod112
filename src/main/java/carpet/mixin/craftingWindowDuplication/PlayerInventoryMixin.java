package carpet.mixin.craftingWindowDuplication;

import carpet.utils.extensions.DupingPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow public PlayerEntity player;

    @Inject(method = "addStack(Lnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void dupeItemSlotStorePartialItemStack(ItemStack itemStackIn, CallbackInfoReturnable<Integer> cir, int slot) {
        ((DupingPlayer) player).dupeItem(slot);
    }

    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I"))
    private void dupeItemSlotAdd(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ((DupingPlayer) player).dupeItem(slot);
    }
}
