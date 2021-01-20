package carpet.mixin.accessors;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Direction.class)
public interface DirectionAccessor {
    @Accessor Vec3i getVector();
    @Accessor("ALL") static Direction[] getValues() { throw new AbstractMethodError(); }
    @Accessor("HORIZONTAL") static Direction[] getHorizontals() { throw new AbstractMethodError(); }
}
