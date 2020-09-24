package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Redirect(method = "processClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack slotClick(Container container, int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        try {
            CarpetServer.playerInventoryStacking.set(Boolean.TRUE);
            return container.slotClick(slotId, dragType, clickTypeIn, player);
        } finally {
            CarpetServer.playerInventoryStacking.set(Boolean.FALSE);
        }
    }
}
