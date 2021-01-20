package carpet.mixin.accessors;

import net.minecraft.class_5569;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_5569.class)
public interface ScoreCriteriaStatAccessor {
    @Accessor("field_26767") Stat getStat();
}
