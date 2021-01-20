package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import net.minecraft.container.Container;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(method = "onClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/Container;onSlotClick(IILnet/minecraft/container/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack slotClick(Container container, int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        try {
            CarpetServer.playerInventoryStacking.set(Boolean.TRUE);
            return container.onSlotClick(slotId, dragType, clickTypeIn, player);
        } finally {
            CarpetServer.playerInventoryStacking.set(Boolean.FALSE);
        }
    }
}
