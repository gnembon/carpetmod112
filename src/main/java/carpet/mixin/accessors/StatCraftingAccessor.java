package carpet.mixin.accessors;

import net.minecraft.item.Item;
import net.minecraft.class_2590;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_2590.class)
public interface StatCraftingAccessor {
    @Accessor("field_32593") Item getItem();
}
