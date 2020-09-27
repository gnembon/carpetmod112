package carpet.mixin.waypoints;

import carpet.utils.Waypoint;
import carpet.utils.extensions.WaypointContainer;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(WorldServer.class)
public class WorldServerMixin implements WaypointContainer {
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
