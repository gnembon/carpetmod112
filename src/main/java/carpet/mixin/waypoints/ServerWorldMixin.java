package carpet.mixin.waypoints;

import carpet.utils.Waypoint;
import carpet.utils.extensions.WaypointContainer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements WaypointContainer {
    private Map<String, Waypoint> waypoints = new LinkedHashMap<>();

    @Override
    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public void setWaypoints(Map<String, Waypoint> waypoints) {
        this.waypoints = waypoints;
    }
}
