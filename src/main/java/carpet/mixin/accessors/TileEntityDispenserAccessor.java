package carpet.mixin.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TileEntityDispenser.class)
public interface TileEntityDispenserAccessor {
    @Accessor NonNullList<ItemStack> getStacks();
}
