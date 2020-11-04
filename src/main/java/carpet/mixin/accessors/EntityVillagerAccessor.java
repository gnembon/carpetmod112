package carpet.mixin.accessors;

import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVillager.class)
public interface EntityVillagerAccessor {
    @Accessor int getWealth();
    @Accessor void setWealth(int wealth);
    @Accessor int getCareerId();
    @Accessor void setCareerId(int id);
    @Accessor int getCareerLevel();
    @Accessor void setCareerLevel(int level);
}
