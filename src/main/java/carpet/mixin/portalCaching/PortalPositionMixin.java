package carpet.mixin.portalCaching;

import carpet.utils.extensions.ExtendedPortalPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PortalForcer.class_5096.class)
public class PortalPositionMixin extends BlockPos implements ExtendedPortalPosition {
    @Shadow public long field_23641;
    private Vec3d cachingCoords;

    public PortalPositionMixin(BlockPos pos, long lastUpdate) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.field_23641 = lastUpdate;
    }

    @Override
    public Vec3d getCachingCoords() {
        return cachingCoords;
    }

    @Override
    public void setCachingCoords(Vec3d coords) {
        this.cachingCoords = coords;
    }
}
