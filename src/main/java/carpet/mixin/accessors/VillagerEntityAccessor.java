package carpet.mixin.accessors;

import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor int getRiches();
    @Accessor void setRiches(int riches);
    @Accessor int getCareer();
    @Accessor void setCareer(int id);
    @Accessor int getCareerLevel();
    @Accessor void setCareerLevel(int level);
}
