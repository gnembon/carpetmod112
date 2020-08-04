package carpet.mixin.accessors;

import net.minecraft.util.WeightedRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedRandom.Item.class)
public interface WeightedRandomItemAccessor {
    @Accessor("itemWeight") int getWeight();
}
