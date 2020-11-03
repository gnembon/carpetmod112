package carpet.mixin.accessors;

import net.minecraft.item.Item;
import net.minecraft.stats.StatCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatCrafting.class)
public interface StatCraftingAccessor {
    @Accessor Item getItem();
}
