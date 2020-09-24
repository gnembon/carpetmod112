package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityItem.class)
public class EntityItemMixin {
    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean addItemStackToInventory(InventoryPlayer inventory, ItemStack stack) {
        try {
            CarpetServer.playerInventoryStacking.set(Boolean.TRUE);
            return inventory.addItemStackToInventory(stack);
        } finally {
            CarpetServer.playerInventoryStacking.set(Boolean.FALSE);
        }
    }
}
