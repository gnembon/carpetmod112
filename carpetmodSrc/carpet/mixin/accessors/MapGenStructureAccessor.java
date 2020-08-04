package carpet.mixin.accessors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapGenStructure.class)
public interface MapGenStructureAccessor {
    @Invoker StructureStart invokeGetStructureAt(BlockPos pos);
}
