package carpet.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Waypoint implements Comparable<Waypoint> {
    public final WorldServer world;
    public final String name;
    public final double x;
    public final double y;
    public final double z;
    public final double yaw;
    public final double pitch;
    public final @Nullable String creator;

    public Waypoint(WorldServer world, String name, @Nullable String creator, double x, double y, double z, double yaw, double pitch) {
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
        return world.provider.getDimensionType();
    }

    public String getFullName() {
        return getDimension().getName() + ":" + name;
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
            // Adapted from spectator teleport code (NetHandlerPlayServer::handleSpectate)
            MinecraftServer server = entity.getServer();
            EntityPlayerMP player = entity instanceof EntityPlayerMP ? (EntityPlayerMP) entity : null;
            WorldServer worldFrom = (WorldServer) entity.world;
            WorldServer worldTo = (WorldServer) this.world;
            int dimension = worldTo.provider.getDimensionType().getId();
            entity.dimension = dimension;
            if (player != null) {
                player.connection.sendPacket(new SPacketRespawn(dimension, worldFrom.getDifficulty(), worldFrom.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
                server.getPlayerList().updatePermissionLevel(player);
            }
            worldFrom.removeEntityDangerously(entity);
            entity.isDead = false;
            entity.setLocationAndAngles(x, y, z, (float) yaw, (float) pitch);

            if (entity.isEntityAlive())
            {
                worldFrom.updateEntityWithOptionalForce(entity, false);
                worldTo.spawnEntity(entity);
                worldTo.updateEntityWithOptionalForce(entity, false);
            }

            entity.setWorld(worldTo);
            if (player != null) {
                server.getPlayerList().preparePlayer(player, worldFrom);
            }
            entity.setPositionAndUpdate(x, y, z);
            if (player != null) {
                player.interactionManager.setWorld(worldTo);
                server.getPlayerList().updateTimeAndWeatherForPlayer(player, worldTo);
                server.getPlayerList().syncPlayerInventory(player);
            }
        } else if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, (float) yaw, (float) pitch);
        } else {
            entity.setLocationAndAngles(x, y, z, (float) yaw, (float) pitch);
        }
    }

    public boolean canManipulate(ICommandSender sender) {
        return sender.canUseCommand(2, "") || (this.creator != null && this.creator.equalsIgnoreCase(sender.getName()));
    }

    public static Set<Waypoint> getAllWaypoints(WorldServer ...worlds) {
        Set<Waypoint> all = new HashSet<>();
        for (WorldServer world : worlds) all.addAll(world.waypoints.values());
        return all;
    }

    @Nullable
    public static Waypoint find(String name, WorldServer defaultWorld, WorldServer... worlds) {
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
            Map<String, Waypoint> waypoints = defaultWorld.waypoints;
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        for (WorldServer world : worlds) {
            if (dimension != null && !dimension.equals(world.provider.getDimensionType())) continue;
            Map<String, Waypoint> waypoints = world.waypoints;
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        return null;
    }

    public static File getWaypointFile(World world) {
        String filename = "waypoints" + world.provider.getDimensionType().getSuffix() + ".json";
        return new File(world.getSaveHandler().getWorldDirectory(), filename);
    }

    public static Gson getGson(MinecraftServer server) {
        return new GsonBuilder()
                .registerTypeAdapter(CollectionAdapter.type, new CollectionAdapter(server))
                .setPrettyPrinting()
                .create();
    };

    public static Map<String, Waypoint> loadWaypoints(WorldServer world) throws IOException {
        File file = getWaypointFile(world);
        if (!file.exists()) return new TreeMap<>();
        Gson gson = getGson(world.getMinecraftServer());
        try(FileReader reader = new FileReader(file)) {
            Collection<Waypoint> waypoints = gson.fromJson(reader, CollectionAdapter.type);
            TreeMap<String, Waypoint> map = new TreeMap<>();
            if (waypoints != null) {
                for (Waypoint w : waypoints) map.put(w.name, w);
            }
            return map;
        }
    }

    public static void saveWaypoints(WorldServer world, Map<String, Waypoint> waypoints) throws IOException {
        File file = getWaypointFile(world);
        if (waypoints.isEmpty()) {
            if (file.exists()) file.delete();
            return;
        }
        Gson gson = getGson(world.getMinecraftServer());
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
                out.name("dimension").value(w.world.provider.getDimensionType().getName());
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
                WorldServer world = server.getWorld(0);
                Double x = null;
                Double y = null;
                Double z = null;
                double yaw = 0;
                double pitch = 0;
                in.beginObject();
                while (in.hasNext()) {
                    switch (in.nextName()) {
                        case "dimension": {
                            world = server.getWorld(DimensionType.byName(in.nextString()).getId());
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
