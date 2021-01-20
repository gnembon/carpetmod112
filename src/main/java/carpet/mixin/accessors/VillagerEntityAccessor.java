package carpet.mixin.accessors;

import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor("field_22462") int getWealth();
    @Accessor("field_22462") void setWealth(int wealth);
    @Accessor("field_22464") int getCareerId();
    @Accessor("field_22464") void setCareerId(int id);
    @Accessor("field_22465") int getCareerLevel();
    @Accessor("field_22465") void setCareerLevel(int level);
}
