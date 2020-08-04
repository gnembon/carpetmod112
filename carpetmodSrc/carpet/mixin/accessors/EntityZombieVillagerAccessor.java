package carpet.mixin.accessors;

import net.minecraft.entity.monster.EntityZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityZombieVillager.class)
public interface EntityZombieVillagerAccessor {
    @Accessor int getConversionTime();
}
