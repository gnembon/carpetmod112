package carpet.mixin.accessors;

import net.minecraft.item.Item;
import net.minecraft.stat.ItemStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStat.class)
public interface StatCraftingAccessor {
    @Accessor Item getItem();
}
