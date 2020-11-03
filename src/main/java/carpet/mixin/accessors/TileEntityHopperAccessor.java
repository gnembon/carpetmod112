package carpet.mixin.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TileEntityHopper.class)
public interface TileEntityHopperAccessor {
    @Invoker static boolean invokeCanCombine(ItemStack a, ItemStack b) { throw new AbstractMethodError(); }
}
