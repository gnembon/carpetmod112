package carpet.mixin.accessors;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnumFacing.class)
public interface EnumFacingAccessor {
    @Accessor Vec3i getDirectionVec();
    @Accessor("VALUES") static EnumFacing[] getValues() { throw new AbstractMethodError(); }
    @Accessor("HORIZONTALS") static EnumFacing[] getHorizontals() { throw new AbstractMethodError(); }
}
