package carpet.utils;

import carpet.utils.extensions.WaypointContainer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Waypoint implements Comparable<Waypoint> {
    public final ServerWorld world;
    public final String name;
    public final double x;
    public final double y;
    public final double z;
    public final double yaw;
    public final double pitch;
    public final @Nullable String creator;

    public Waypoint(ServerWorld world, String name, @Nullable String creator, double x, double y, double z, double yaw, double pitch) {
        this.world = world;
        this.creator = creator;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public DimensionType getDimension() {
        return world.dimension.getType();
    }

    public String getFullName() {
        return getDimension().method_27531() + ":" + name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == Waypoint.class && ((Waypoint) obj).world == this.world && Objects.equals(((Waypoint) obj).name, this.name);
    }

    @Override
    public int hashCode() {
        return world.hashCode() ^ name.hashCode();
    }

    @Override
    public int compareTo(Waypoint o) {
        if (o == null) return -1;
        if (this.equals(o)) return 0;
        if (this.world == o.world) return name.compareTo(o.name);
        return getFullName().compareTo(o.getFullName());
    }

    @Override
    public String toString() {
        return String.format("Waypoint[%s,(%f,%f,%f),(%f, %f)", getFullName(), x, y, z, yaw, pitch);
    }

    public void teleport(Entity entity) {
        if (entity.world != this.world) {
            // Adapted from spectator teleport code (ServerPlayNetworkHandler::onSpectatorTeleport)
            MinecraftServer server = entity.getServer();
            ServerPlayerEntity player = entity instanceof ServerPlayerEntity ? (ServerPlayerEntity) entity : null;
            ServerWorld worldFrom = (ServerWorld) entity.world;
            ServerWorld worldTo = this.world;
            int dimension = worldTo.dimension.getType().getRawId();
            entity.dimensionId = dimension;
            if (player != null) {
                player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(dimension, worldFrom.getDifficulty(), worldFrom.getLevelProperties().getGeneratorType(), player.interactionManager.getGameMode()));
                server.getPlayerManager().sendCommandTree(player);
            }
            worldFrom.removeEntity(entity);
            worldFrom.method_25975(entity.chunkX, entity.chunkZ).remove(entity, entity.chunkY);
            entity.removed = false;
            entity.refreshPositionAndAngles(x, y, z, (float) yaw, (float) pitch);

            if (entity.isAlive())
            {
                worldTo.spawnEntity(entity);
                worldTo.method_26050(entity, false);
            }

            entity.setWorld(worldTo);
            if (player != null) {
                server.getPlayerManager().method_33707(player, worldFrom);
            }
            entity.requestTeleport(x, y, z);
            if (player != null) {
                player.interactionManager.setWorld(worldTo);
                server.getPlayerManager().sendWorldInfo(player, worldTo);
                server.getPlayerManager().method_33734(player);
            }
        } else if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).networkHandler.requestTeleport(x, y, z, (float) yaw, (float) pitch);
        } else {
            entity.refreshPositionAndAngles(x, y, z, (float) yaw, (float) pitch);
        }
    }

    public boolean canManipulate(CommandSource sender) {
        return sender.allowCommandExecution(2, "") || (this.creator != null && this.creator.equalsIgnoreCase(sender.getName()));
    }

    public static Set<Waypoint> getAllWaypoints(ServerWorld ...worlds) {
        Set<Waypoint> all = new HashSet<>();
        for (ServerWorld world : worlds) all.addAll(((WaypointContainer) world).getWaypoints().values());
        return all;
    }

    @Nullable
    public static Waypoint find(String name, ServerWorld defaultWorld, ServerWorld... worlds) {
        DimensionType dimension = null;
        int colon = name.indexOf(':');
        if (colon >= 0) {
            String dimStr = name.substring(0, colon);
            switch (dimStr) {
                case "overworld": dimension = DimensionType.OVERWORLD; break;
                case "nether": case "the_nether": dimension = DimensionType.NETHER; break;
                case "end": case "the_end": dimension = DimensionType.THE_END; break;
            }
            if (dimension != null) {
                name = name.substring(colon + 1);
            }
        }
        if (dimension == null) {
            Map<String, Waypoint> waypoints = ((WaypointContainer) defaultWorld).getWaypoints();
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        for (ServerWorld world : worlds) {
            if (dimension != null && !dimension.equals(world.dimension.getType())) continue;
            Map<String, Waypoint> waypoints = ((WaypointContainer) world).getWaypoints();
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        return null;
    }

    public static File getWaypointFile(World world) {
        String filename = "waypoints" + world.dimension.getType().getSuffix() + ".json";
        return new File(world.method_25960().method_28318(), filename);
    }

    public static Gson getGson(MinecraftServer server) {
        return new GsonBuilder()
                .registerTypeAdapter(CollectionAdapter.type, new CollectionAdapter(server))
                .setPrettyPrinting()
                .create();
    };

    public static Map<String, Waypoint> loadWaypoints(ServerWorld world) throws IOException {
        File file = getWaypointFile(world);
        if (!file.exists()) return new TreeMap<>();
        Gson gson = getGson(world.getServer());
        try(FileReader reader = new FileReader(file)) {
            Collection<Waypoint> waypoints = gson.fromJson(reader, CollectionAdapter.type);
            TreeMap<String, Waypoint> map = new TreeMap<>();
            if (waypoints != null) {
                for (Waypoint w : waypoints) map.put(w.name, w);
            }
            return map;
        }
    }

    public static void saveWaypoints(ServerWorld world, Map<String, Waypoint> waypoints) throws IOException {
        File file = getWaypointFile(world);
        if (waypoints.isEmpty()) {
            if (file.exists()) file.delete();
            return;
        }
        Gson gson = getGson(world.getServer());
        try(FileWriter writer = new FileWriter(file)) {
            gson.toJson(waypoints.values(), CollectionAdapter.type, writer);
        }
    }

    public static class CollectionAdapter extends TypeAdapter<Collection<Waypoint>> {
        public static final Type type = new TypeToken<Collection<Waypoint>>(){}.getType();
        private MinecraftServer server;

        public CollectionAdapter(MinecraftServer server) {
            this.server = server;
        }

        @Override
        public void write(JsonWriter out, Collection<Waypoint> waypoints) throws IOException {
            out.beginObject();
            for (Waypoint w : waypoints) {
                out.name(w.name);
                out.beginObject();
                out.name("dimension").value(w.world.dimension.getType().method_27531());
                out.name("x").value(w.x);
                out.name("y").value(w.y);
                out.name("z").value(w.z);
                out.name("yaw").value(w.yaw);
                out.name("pitch").value(w.pitch);
                out.name("creator").value(w.creator);
                out.endObject();
            }
            out.endObject();
        }

        @Override
        public Collection<Waypoint> read(JsonReader in) throws IOException {
            List<Waypoint> waypoints = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                String creator = null;
                ServerWorld world = server.getWorldById(0);
                Double x = null;
                Double y = null;
                Double z = null;
                double yaw = 0;
                double pitch = 0;
                in.beginObject();
                while (in.hasNext()) {
                    switch (in.nextName()) {
                        case "dimension": {
                            world = server.getWorldById(DimensionType.method_27530(in.nextString()).getRawId());
                            break;
                        }
                        case "x": {
                            x = in.nextDouble();
                            break;
                        }
                        case "y": {
                            y = in.nextDouble();
                            break;
                        }
                        case "z": {
                            z = in.nextDouble();
                            break;
                        }
                        case "yaw": {
                            yaw = in.nextDouble();
                            break;
                        }
                        case "pitch": {
                            pitch = in.nextDouble();
                            break;
                        }
                        case "creator": {
                            creator = in.nextString();
                            break;
                        }
                    }
                }
                in.endObject();
                if (x != null && y != null && z != null) {
                    waypoints.add(new Waypoint(world, name, creator, x, y, z, yaw, pitch));
                }
            }
            in.endObject();
            return waypoints;
        }
    }
}
