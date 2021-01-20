package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {
    @Accessor("field_26039") Long2ObjectMap<StructureStart> getStructureMap();
    @Invoker("method_27843") StructureStart invokeGetStructureAt(BlockPos pos);
}
