package carpet.mixin.autoCraftingTable;

import carpet.helpers.TileEntityCraftingTable;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin {
    @Shadow private static void register(String id, Class<? extends TileEntity> clazz) {}

    @Inject(method = "<clinit>()V", at = @At("RETURN"))
    private void registerAutoCraftingTable() {
        register("crafting_table", TileEntityCraftingTable.class);
    }
}
