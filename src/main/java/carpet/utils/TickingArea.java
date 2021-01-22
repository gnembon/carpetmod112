package carpet.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import carpet.mixin.accessors.ThreadedAnvilChunkStorageAccessor;
import carpet.mixin.accessors.ServerChunkCacheAccessor;
import carpet.mixin.accessors.MinecraftServerAccessor;
import carpet.utils.extensions.WorldWithTickingAreas;
import net.minecraft.class_5318;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkCache;

public abstract class TickingArea
{
    private static final Logger LOGGER = LogManager.getLogger();
    
    // TICKING AREA LIST
    
    public static boolean isTickingChunk(World world, int chunkX, int chunkZ)
    {
        return ((WorldWithTickingAreas) world).getTickingChunks().contains(ColumnPos.method_25891(chunkX, chunkZ));
    }
    
    public static List<TickingArea> getTickingAreas(World world)
    {
        return Collections.unmodifiableList(((WorldWithTickingAreas) world).getTickingAreas());
    }
    
    public static void addTickingArea(World world, TickingArea area)
    {
        ((WorldWithTickingAreas) world).getTickingAreas().add(area);
        for (ColumnPos pos : area.listIncludedChunks(world))
        {
            ((WorldWithTickingAreas) world).getTickingChunks().add(ColumnPos.method_25891(pos.x, pos.z));
        }
    }
    
    public static boolean removeTickingAreas(World world, int chunkX, int chunkZ)
    {
        return removeTickingAreaIf(world, area -> area.contains(world, chunkX, chunkZ));
    }
    
    public static boolean removeTickingAreas(World world, String name)
    {
        return removeTickingAreaIf(world, area -> Objects.equals(name, area.getName()));
    }
    
    private static boolean removeTickingAreaIf(World world, Predicate<TickingArea> predicate)
    {
        boolean anyRemoved = false;
        Iterator<TickingArea> itr = ((WorldWithTickingAreas) world).getTickingAreas().iterator();
        while (itr.hasNext())
        {
            TickingArea area = itr.next();
            if (predicate.test(area))
            {
                itr.remove();
                anyRemoved = true;
                for (ColumnPos chunk : area.listIncludedChunks(world))
                {
                    if (((WorldWithTickingAreas) world).getTickingAreas().stream().noneMatch(a -> a.contains(world, chunk.x, chunk.z)))
                    {
                        ((WorldWithTickingAreas) world).getTickingChunks().remove(ColumnPos.method_25891(chunk.x, chunk.z));
                    }
                }
            }
        }
        return anyRemoved;
    }
    
    public static void removeAllTickingAreas(World world)
    {
        ((WorldWithTickingAreas) world).getTickingAreas().clear();
        ((WorldWithTickingAreas) world).getTickingChunks().clear();
    }
    
    public static boolean hasTickingArea(World world)
    {
        return !((WorldWithTickingAreas) world).getTickingAreas().isEmpty();
    }
    
    // CONFIG
    
    public static void loadConfig(MinecraftServer server)
    {
        for (World world : server.worlds)
        {
            removeAllTickingAreas(world);
            
            ChunkCache chunkProvider = world.getChunkManager();
            if (!(chunkProvider instanceof ServerChunkCache))
            {
                continue;
            }
            class_5318 chunkLoader = ((ServerChunkCacheAccessor) chunkProvider).getChunkLoader();
            if (!(chunkLoader instanceof ThreadedAnvilChunkStorage))
            {
                continue;
            }
            
            File configFile = new File(((ThreadedAnvilChunkStorageAccessor) chunkLoader).getChunkSaveLocation(), "tickingareas.conf");
            if (!configFile.isFile())
            {
                continue;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile)))
            {
                reader.lines().forEach(line -> {
                    String[] args = line.split(" ");
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
                        LOGGER.error("[CM]: Invalid ticking area type in config file, skipping");
                        return;
                    }
                    if (!area.readFromConfig(args))
                    {
                        LOGGER.error("[CM]: Error in ticking area parameters, skipping");
                        return;
                    }
                    addTickingArea(world, area);
                });
            }
            catch (IOException e)
            {
                LOGGER.error("[CM]: Couldn't load ticking area config", e);
            }
        }
    }
    
    public static void saveConfig(MinecraftServer server)
    {
        for (World world : server.worlds)
        {
            ChunkCache chunkProvider = world.getChunkManager();
            if (!(chunkProvider instanceof ServerChunkCache))
                continue;
            class_5318 chunkLoader = ((ServerChunkCacheAccessor) chunkProvider).getChunkLoader();
            if (!(chunkLoader instanceof ThreadedAnvilChunkStorage))
                continue;
            
            File configFile = new File(((ThreadedAnvilChunkStorageAccessor) chunkLoader).getChunkSaveLocation(), "tickingareas.conf");
            
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(configFile))))
            {
                for (TickingArea area : ((WorldWithTickingAreas) world).getTickingAreas())
                {
                    String val = String.join(" ", Arrays.stream(area.writeToConfig()).filter(arg -> arg != null).toArray(String[]::new));
                    writer.println(val);
                }
                writer.flush();
            }
            catch (IOException e)
            {
                LOGGER.error("[CM]: Couldn't save ticking area config", e);
            }
        }
    }
    
    // GLOBAL UTILITY METHODS
    
    public static void initialChunkLoad(MinecraftServer server, boolean log)
    {
        for (ServerWorld world : server.worlds)
        {
            if (TickingArea.hasTickingArea(world))
            {
                if (log)
                    LOGGER.info("[CM]: Preparing start region for level " + world.dimension.getType().getRawId());
                
                TreeSet<ColumnPos> chunksToLoad = new TreeSet<>(Comparator.<ColumnPos>comparingInt(chunk -> chunk.x).thenComparingInt(chunk -> chunk.z));
                for (TickingArea area : TickingArea.getTickingAreas(world))
                {
                    chunksToLoad.addAll(area.listIncludedChunks(world));
                }
                
                int count = 0;
                long lastTime = MinecraftServer.getMeasuringTimeMs();
                for (ColumnPos chunk : chunksToLoad)
                {
                    if (!server.isRunning())
                        break;
                    
                    long time = MinecraftServer.getMeasuringTimeMs();
                    
                    if (time - lastTime > 1000)
                    {
                        if (log)
                            ((MinecraftServerAccessor) server).invokeLogPercentage("[CM]: Preparing spawn area", count * 100 / chunksToLoad.size());
                        lastTime = time;
                    }
                    
                    count++;
                    world.getChunkManager().method_27347(chunk.x, chunk.z);
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
    
    public abstract List<ColumnPos> listIncludedChunks(World world);
    
    public abstract String format();
    
    public abstract String[] writeToConfig();
    
    public abstract boolean readFromConfig(String[] args);
    
    // TICKING AREA TYPES
    
    public static class SpawnChunks extends TickingArea
    {
        @Override
        protected boolean contains(World world, int chunkX, int chunkZ)
        {
            return world.method_26115(chunkX, chunkZ);
        }
        
        @Override
        public List<ColumnPos> listIncludedChunks(World world)
        {
            BlockPos spawnPoint = world.getSpawnPos();
            int spawnChunkX = spawnPoint.getX() / 16;
            int spawnChunkZ = spawnPoint.getZ() / 16;
            
            List<ColumnPos> spawnChunks = new ArrayList<>();
            
            for (int x = spawnChunkX - 9; x <= spawnChunkX + 9; x++)
            {
                for (int z = spawnChunkZ - 9; z <= spawnChunkZ + 9; z++)
                {
                    if (world.method_26115(x, z))
                    {
                        spawnChunks.add(new ColumnPos(x, z));
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
            return new String[] {"spawnChunks", getName()};
        }

        @Override
        public boolean readFromConfig(String[] args)
        {
            if (args.length > 1)
                setName(Arrays.stream(args).skip(1).collect(Collectors.joining(" ")));
            return true;
        }
    }
    
    public static class Square extends TickingArea
    {
        private ColumnPos min;
        private ColumnPos max;
        
        public Square() {}
        
        public Square(ColumnPos min, ColumnPos max)
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
        public List<ColumnPos> listIncludedChunks(World world)
        {
            List<ColumnPos> includedChunks = new ArrayList<>();
            
            for (int x = min.x; x <= max.x; x++)
            {
                for (int z = min.z; z <= max.z; z++)
                {
                    includedChunks.add(new ColumnPos(x, z));
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
            return new String[] {"square", String.valueOf(min.x), String.valueOf(min.z), String.valueOf(max.x), String.valueOf(max.z), getName()};
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
                this.min = new ColumnPos(minX, minZ);
                this.max = new ColumnPos(maxX, maxZ);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
            
            if (args.length > 5)
                setName(Arrays.stream(args).skip(5).collect(Collectors.joining(" ")));
            
            return true;
        }
    }
    
    public static class Circle extends TickingArea
    {
        private ColumnPos center;
        private double radius;
        
        public Circle() {}
        
        public Circle(ColumnPos center, double radius)
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
        public List<ColumnPos> listIncludedChunks(World world)
        {
            List<ColumnPos> includedChunks = new ArrayList<>();
            
            for (int x = MathHelper.floor(center.x - radius); x <= center.x + radius; x++)
            {
                for (int z = MathHelper.floor(center.z - radius); z <= center.z + radius; z++)
                {
                    if (contains(world, x, z))
                    {
                        includedChunks.add(new ColumnPos(x, z));
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
            return new String[] {"circle", String.valueOf(center.x), String.valueOf(center.z), String.valueOf(radius), getName()};
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
                
                this.center = new ColumnPos(centerX, centerZ);
                this.radius = radius;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
            
            if (args.length > 4)
                setName(Arrays.stream(args).skip(4).collect(Collectors.joining(" ")));
            
            return true;
        }
    }
}
