package carpet.utils.extensions;

import net.minecraft.util.math.Vec3d;

public interface ExtendedPortalPosition {
    Vec3d getCachingCoords();
    void setCachingCoords(Vec3d coords);
}
