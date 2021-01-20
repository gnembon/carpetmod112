package carpet.mixin.autoCraftingTable;

import carpet.helpers.ContainerAutoCraftingTable;
import net.minecraft.class_4522;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_4522.class)
public class ServerRecipeBookHelperMixin {
    @Redirect(method = "method_33169", at = @At(value = "FIELD", target = "Lnet/minecraft/container/CraftingTableContainer;craftingInv:Lnet/minecraft/inventory/CraftingInventory;"))
    private CraftingInventory getCraftMatrix(CraftingTableContainer container) {
        return container instanceof ContainerAutoCraftingTable ? ((ContainerAutoCraftingTable) container).getInventoryCrafting() : container.craftingInv;
    }
}
