package carpet.mixin.autoCraftingTable;

import carpet.helpers.ContainerAutoCraftingTable;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.ServerRecipeBookHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerRecipeBookHelper.class)
public class ServerRecipeBookHelperMixin {
    @Redirect(method = "func_194327_a", at = @At(value = "FIELD", target = "Lnet/minecraft/inventory/ContainerWorkbench;craftMatrix:Lnet/minecraft/inventory/InventoryCrafting;"))
    private InventoryCrafting getCraftMatrix(ContainerWorkbench container) {
        return container instanceof ContainerAutoCraftingTable ? ((ContainerAutoCraftingTable) container).getInventoryCrafting() : container.craftMatrix;
    }
}
