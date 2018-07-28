package carpet.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carpet.CarpetServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class TickingArea
{
    private static final Logger LOGGER = LogManager.getLogger();
    
    // TICKING AREA LIST
    
    private static Map<World, List<TickingArea>> tickingAreas = new WeakHashMap<>();
    
    public static boolean isTickingChunk(World world, int chunkX, int chunkZ)
    {
        return tickingAreas(world).stream().anyMatch(area -> area.contains(world, chunkX, chunkZ));
    }
    
    private static List<TickingArea> tickingAreas(World world)
    {
        List<TickingArea> areas = tickingAreas.get(world);
        if (areas == null)
        {
            areas = new ArrayList<>();
            tickingAreas.put(world, areas);
        }
        return areas;
    }
    
    public static List<TickingArea> getTickingAreas(World world)
    {
        return Collections.unmodifiableList(tickingAreas(world));
    }
    
    public static void addTickingArea(World world, TickingArea area)
    {
        tickingAreas(world).add(area);
    }
    
    public static boolean removeTickingAreas(World world, int chunkX, int chunkZ)
    {
        return tickingAreas(world).removeIf(area -> area.contains(world, chunkX, chunkZ));
    }
    
    public static boolean removeTickingAreas(World world, String name)
    {
        return tickingAreas(world).removeIf(area -> name.equals(area.getName()));
    }
    
    public static void removeAllTickingAreas(World world)
    {
        tickingAreas(world).clear();
    }
    
    public static boolean hasTickingArea(World world)
    {
        return !tickingAreas(world).isEmpty();
    }
    
    public static void readFromConfig(Map<String, String> config)
    {
        for (World world : CarpetServer.minecraft_server.worlds)
        {
            String dim = world.provider.getDimensionType().getName();
            int count;
            try
            {
                count = Integer.parseInt(config.getOrDefault(dim + "_ticking_areas", "0"));
            }
            catch (NumberFormatException e)
            {
                count = 0;
            }
            config.remove(dim + "_ticking_areas");
            
            List<TickingArea> areas = tickingAreas(world);
            for (int i = 0; i < count; i++)
            {
                String val = config.remove(dim + "_ticking_area_" + i);
                if (val == null)
                    continue;
                
                String[] args = val.split(" ");
                TickingArea area;
                switch (args[0])
                {
                case "square":
                    area = new Square();
                    break;
                case "circle":
                    area = new Circle();
                    break;
                case "spawnChunks":
                    area = new SpawnChunks();
                    break;
                default:
                    continue;
                }
                
                if (!area.readFromConfig(args))
                    continue;
                
                areas.add(area);
            }
        }
    }
    
    public static void writeToConfig(Map<String, String> config)
    {
        for (World world : CarpetServer.minecraft_server.worlds)
        {
            if (hasTickingArea(world))
            {
                List<TickingArea> areas = tickingAreas(world);
                String dim = world.provider.getDimensionType().getName();
                config.put(dim + "_ticking_areas", String.valueOf(areas.size()));
                for (int i = 0; i < areas.size(); i++)
                {
                    TickingArea area = areas.get(i);
                    String val = String.join(" ", area.writeToConfig());
                    config.put(dim + "_ticking_area_" + i, val);
                }
            }
        }
    }
    
    // GLOBAL UTILITY METHODS
    
    public static void initialChunkLoad(MinecraftServer server, boolean log)
    {
        for (WorldServer world : server.worlds)
        {
            if (TickingArea.hasTickingArea(world))
            {
                if (log)
                    LOGGER.info("Preparing start region for level " + world.provider.getDimensionType().getId());
                
                TreeSet<ChunkPos> chunksToLoad = new TreeSet<>(Comparator.<ChunkPos>comparingInt(chunk -> chunk.x).thenComparingInt(chunk -> chunk.z));
                for (TickingArea area : TickingArea.getTickingAreas(world))
                {
                    chunksToLoad.addAll(area.listIncludedChunks(world));
                }
                
                int count = 0;
                long lastTime = MinecraftServer.getCurrentTimeMillis();
                for (ChunkPos chunk : chunksToLoad)
                {
                    if (!server.isServerRunning())
                        break;
                    
                    long time = MinecraftServer.getCurrentTimeMillis();
                    
                    if (time - lastTime > 1000)
                    {
                        if (log)
                            server.outputPercentRemaining("Preparing spawn area", count * 100 / chunksToLoad.size());
                        lastTime = time;
                    }
                    
                    count++;
                    world.getChunkProvider().provideChunk(chunk.x, chunk.z);
                }
            }
        }
    }
    
    // INSTANCE METHODS
    
    private String name = null;
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    protected abstract boolean contains(World world, int chunkX, int chunkZ);
    
    public abstract List<ChunkPos> listIncludedChunks(World world);
    
    public abstract String format();
    
    public abstract String[] writeToConfig();
    
    public abstract boolean readFromConfig(String[] args);
    
    // TICKING AREA TYPES
    
    public static class SpawnChunks extends TickingArea
    {
        @Override
        protected boolean contains(World world, int chunkX, int chunkZ)
        {
            return world.isSpawnChunk(chunkX, chunkZ);
        }
        
        @Override
        public List<ChunkPos> listIncludedChunks(World world)
        {
            BlockPos spawnPoint = world.getSpawnPoint();
            int spawnChunkX = spawnPoint.getX() / 16;
            int spawnChunkZ = spawnPoint.getZ() / 16;
            
            List<ChunkPos> spawnChunks = new ArrayList<>();
            
            for (int x = spawnChunkX - 9; x <= spawnChunkX + 9; x++)
            {
                for (int z = spawnChunkZ - 9; z <= spawnChunkZ + 9; z++)
                {
                    if (world.isSpawnChunk(x, z))
                    {
                        spawnChunks.add(new ChunkPos(x, z));
                    }
                }
            }
            
            return spawnChunks;
        }
        
        @Override
        public String format()
        {
            return "Spawn Chunks";
        }

        @Override
        public String[] writeToConfig()
        {
            return new String[] {"spawnChunks"};
        }

        @Override
        public boolean readFromConfig(String[] args)
        {
            return true;
        }
    }
    
    public static class Square extends TickingArea
    {
        private ChunkPos min;
        private ChunkPos max;
        
        public Square() {}
        
        public Square(ChunkPos min, ChunkPos max)
        {
            this.min = min;
            this.max = max;
        }
        
        @Override
        protected boolean contains(World world, int chunkX, int chunkZ)
        {
            return chunkX >= min.x && chunkX <= max.x
                && chunkZ >= min.z && chunkZ <= max.z;
        }
        
        @Override
        public List<ChunkPos> listIncludedChunks(World world)
        {
            List<ChunkPos> includedChunks = new ArrayList<>();
            
            for (int x = min.x; x <= max.x; x++)
            {
                for (int z = min.z; z <= max.z; z++)
                {
                    includedChunks.add(new ChunkPos(x, z));
                }
            }
            
            return includedChunks;
        }
        
        @Override
        public String format()
        {
            return "Square, (" + min.x + ", " + min.z + ") to (" + max.x + ", " + max.z + ")";
        }

        @Override
        public String[] writeToConfig()
        {
            return new String[] {"square", String.valueOf(min.x), String.valueOf(min.z), String.valueOf(max.x), String.valueOf(max.z)};
        }

        @Override
        public boolean readFromConfig(String[] args)
        {
            if (args.length < 5)
                return false;
            
            try
            {
                int minX = Integer.parseInt(args[1]);
                int minZ = Integer.parseInt(args[2]);
                int maxX = Integer.parseInt(args[3]);
                int maxZ = Integer.parseInt(args[4]);
                if (minX > maxX)
                    return false;
                if (minZ > maxZ)
                    return false;
                this.min = new ChunkPos(minX, minZ);
                this.max = new ChunkPos(maxX, maxZ);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
            
            return true;
        }
    }
    
    public static class Circle extends TickingArea
    {
        private ChunkPos center;
        private double radius;
        
        public Circle() {}
        
        public Circle(ChunkPos center, double radius)
        {
            this.center = center;
            this.radius = radius;
        }
        
        @Override
        protected boolean contains(World world, int chunkX, int chunkZ)
        {
            int dx = center.x - chunkX;
            int dz = center.z - chunkZ;
            return dx * dx + dz * dz <= radius * radius;
        }
        
        @Override
        public List<ChunkPos> listIncludedChunks(World world)
        {
            List<ChunkPos> includedChunks = new ArrayList<>();
            
            for (int x = MathHelper.floor(center.x - radius); x <= center.x + radius; x++)
            {
                for (int z = MathHelper.floor(center.z - radius); z <= center.z + radius; z++)
                {
                    if (contains(world, x, z))
                    {
                        includedChunks.add(new ChunkPos(x, z));
                    }
                }
            }
            
            return includedChunks;
        }
        
        @Override
        public String format()
        {
            return "Circle, (" + center.x + ", " + center.z + "), radius " + radius;
        }

        @Override
        public String[] writeToConfig()
        {
            return new String[] {"circle", String.valueOf(center.x), String.valueOf(center.z), String.valueOf(radius)};
        }

        @Override
        public boolean readFromConfig(String[] args)
        {
            if (args.length < 4)
                return false;
            
            try
            {
                int centerX = Integer.parseInt(args[1]);
                int centerZ = Integer.parseInt(args[2]);
                double radius = Double.parseDouble(args[3]);
                if (radius < 0)
                    return false;
                
                this.center = new ChunkPos(centerX, centerZ);
                this.radius = radius;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
            
            return true;
        }
    }
}
