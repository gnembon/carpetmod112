package carpet.mixin.accessors;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
public interface HopperBlockEntityAccessor {
    @Invoker static boolean invokeCanMergeItems(ItemStack a, ItemStack b) { throw new AbstractMethodError(); }
}
