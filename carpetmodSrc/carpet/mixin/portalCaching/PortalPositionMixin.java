package carpet.mixin.portalCaching;

import carpet.utils.extensions.ExtendedPortalPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Teleporter.PortalPosition.class)
public class PortalPositionMixin extends BlockPos implements ExtendedPortalPosition {
    @Shadow public long lastUpdateTime;
    private Vec3d cachingCoords;

    public PortalPositionMixin(BlockPos pos, long lastUpdate, Vec3d cachingCoords) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.lastUpdateTime = lastUpdate;
        this.cachingCoords = cachingCoords;
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
