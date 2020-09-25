package carpet.utils.extensions;

import carpet.utils.Waypoint;

import java.util.Map;

public interface WaypointContainer {
    Map<String, Waypoint> getWaypoints();
    void setWaypoints(Map<String, Waypoint> waypoints);
}
