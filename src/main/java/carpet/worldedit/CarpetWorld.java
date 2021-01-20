package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import carpet.mixin.accessors.ServerChunkManagerAccessor;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.class_5305;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
@SuppressWarnings("deprecation")
class CarpetWorld extends AbstractWorld {

    private static final Logger logger = Logger.getLogger(CarpetWorld.class.getCanonicalName());
    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    CarpetWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<World>(world);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws WorldEditException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorldChecked() throws WorldEditException {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldReferenceLostException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorld() {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return getWorld().getLevelProperties().getLevelName();
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        WorldChunk chunk = world.method_25975(x >> 4, z >> 4);
        BlockState oldState = Blocks.AIR.getDefaultState();

        if (notifyAndLight) {
            oldState = chunk.method_27361(x & 15, y, z & 15);
        }

        BlockState newState = Block.getBlockFromRawId(block.getId()).getDefaultState(block.getData());
        boolean successful = chunk.method_27373(new BlockPos(x & 15, y, z & 15), newState) != null;

        // Create the TileEntity
        if (successful) {
            CompoundTag tag = block.getNbtData();
            if (tag != null) {
                net.minecraft.nbt.CompoundTag nativeTag = NBTConverter.toNative(tag);
                nativeTag.putString("id", block.getNbtId());
                TileEntityUtils.setTileEntity(getWorld(), position, nativeTag);
            }
        }

        BlockPos pos = new BlockPos(x, y, z);
        if (notifyAndLight) {
            if (newState.method_27191() != oldState.method_27191() || newState.getLuminance() != oldState.getLuminance())
                world.method_26153(pos);
        }
        world.updateListeners(pos, oldState, newState, 3);
        if (notifyAndLight) {
            world.method_26017(pos, oldState.getBlock(), true);

            if (newState.method_27209()) {
                world.updateHorizontalAdjacent(pos, newState.getBlock());
            }
        }

        return successful;
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        checkNotNull(position);
        return getWorld().getLightLevel(LightType.BLOCK, new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        checkNotNull(position);
        BlockEntity tile = getWorld().getBlockEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        if ((tile instanceof Inventory)) {
            Inventory inv = (Inventory) tile;
            int size = inv.getInvSize();
            for (int i = 0; i < size; i++) {
                inv.setInvStack(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        checkNotNull(position);
        return new BaseBiome(Biome.method_26235(getWorld().getBiome(new BlockPos(position.getBlockX(), 0, position.getBlockZ()))));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        checkNotNull(position);
        checkNotNull(biome);

        BlockPos pos = new BlockPos(position.getBlockX(), 0, position.getBlockZ());
        if (getWorld().canSetBlock(pos)) {
            WorldChunk chunk = getWorld().getWorldChunk(pos);
            chunk.method_27418()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = (byte) biome.getId();
            return true;
        }

        return false;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == 0) {
            return;
        }

        ItemEntity entity = new ItemEntity(getWorld(), position.getX(), position.getY(), position.getZ(), CarpetWorldEdit.toCarpetItemStack(item));
        entity.setToDefaultPickupDelay();
        getWorld().spawnEntity(entity);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        BaseBlock[] history = new BaseBlock[256 * (getMaxY() + 1)];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < getMaxY() + 1; y++) {
                    for (int z = 0; z < 16; z++) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = editSession.getBlock(pt);
                    }
                }
            }
            try {
                Set<Vector2D> chunks = region.getChunks();
                class_5305 provider = getWorld().getChunkManager();
                if (!(provider instanceof ServerChunkManager)) {
                    return false;
                }
                ServerChunkManager chunkServer = (ServerChunkManager) provider;
                /*
                Field u;
                try {
                    u = ChunkProviderServer.class.getDeclaredField("field_73248_b"); // chunksToUnload
                } catch(NoSuchFieldException e) {
                    u = ChunkProviderServer.class.getDeclaredField("chunksToUnload");
                }
                u.setAccessible(true);
                */
                Set<Long> unloadQueue = ((ServerChunkManagerAccessor) chunkServer).getDroppedChunks();
                /*
                Field m;
                try {
                    m = ChunkProviderServer.class.getDeclaredField("field_73244_f"); // loadedChunkHashMap
                } catch(NoSuchFieldException e) {
                    m = ChunkProviderServer.class.getDeclaredField("loadedChunkHashMap");
                }
                m.setAccessible(true);
                */
                Long2ObjectMap<WorldChunk> loadedMap = ((ServerChunkManagerAccessor) chunkServer).getLoadedChunksMap();
                /*
                Field lc;
                try {
                    lc = ChunkProviderServer.class.getDeclaredField("field_73245_g"); // loadedChunkHashMap
                } catch(NoSuchFieldException e) {
                    lc = ChunkProviderServer.class.getDeclaredField("loadedChunks");
                }
                lc.setAccessible(true);
                @SuppressWarnings("unchecked") List<Chunk> loaded = (List<Chunk>) lc.get(chunkServer);
                */
                /*
                Field p;
                try {
                    p = ChunkProviderServer.class.getDeclaredField("field_73246_d"); // currentChunkProvider
                } catch(NoSuchFieldException e) {
                    p = ChunkProviderServer.class.getDeclaredField("currentChunkProvider");
                }
                p.setAccessible(true);
                */
                //IChunkGenerator chunkProvider = chunkServer.chunkGenerator;

                for (Vector2D coord : chunks) {
                    long pos = ColumnPos.method_25891(coord.getBlockX(), coord.getBlockZ());
                    WorldChunk mcChunk;
                    if (chunkServer.method_33456(coord.getBlockX(), coord.getBlockZ())) {
                        mcChunk = chunkServer.method_33452(coord.getBlockX(), coord.getBlockZ());
                        mcChunk.method_27398();
                    }
                    unloadQueue.remove(pos);
                    loadedMap.remove(pos);
                    mcChunk = chunkServer.method_27347(coord.getBlockX(), coord.getBlockZ());
                    loadedMap.put(pos, mcChunk);
                    //loaded.add(mcChunk);
                    /*
                    if (mcChunk != null) {
                        mcChunk.onChunkLoad();
                        mcChunk.populateChunk(chunkProvider, chunkProvider, coord.getBlockX(), coord.getBlockZ());
                    }
                    */
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Failed to generate chunk", t);
                return false;
            }

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < getMaxY() + 1; y++) {
                    for (int z = 0; z < 16; z++) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        if (!region.contains(pt))
                            editSession.smartSetBlock(pt, history[index]);
                        else {
                            editSession.rememberChange(pt, history[index], editSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public WorldData getWorldData() {
        return CarpetWorldData.getInstance();
    }

    @Override
    public boolean isValidBlockType(int id) {
        return (id == 0) || (net.minecraft.block.Block.getBlockFromRawId(id) != null);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        World world = getWorld();
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getZ());
        BlockState state = world.getBlockState(pos);
        int id = Block.getId(state.getBlock());
        int data = state.getBlock().getMeta(state);
        BlockEntity tile = getWorld().getBlockEntity(pos);

        if (tile != null) {
            return new TileEntityBaseBlock(id, data, tile);
        } else {
            return new BaseBlock(id, data);
        }
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        BlockState state = world.getBlockState(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        int id = Block.getId(state.getBlock());
        int data = state.getBlock().getMeta(state);
        return new LazyBlock(id, data, this, position);
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if ((o instanceof CarpetWorld)) {
            CarpetWorld other = ((CarpetWorld) o);
            World otherWorld = other.worldRef.get();
            World thisWorld = worldRef.get();
            return otherWorld != null && thisWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList();
        World world = getWorld();
        List<net.minecraft.entity.Entity> ents = world.field_23572;
        for (net.minecraft.entity.Entity entity : ents) {
            if (region.contains(new Vector(entity.x, entity.y, entity.z))) {
                entities.add(new CarpetEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList();
        for (Object entity : getWorld().field_23572) {
            entities.add(new CarpetEntity((net.minecraft.entity.Entity) entity));
        }
        return entities;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();
        net.minecraft.entity.Entity createdEntity = EntityType.createInstanceFromId(new Identifier(entity.getTypeId()), world);
        if (createdEntity != null) {
            CompoundTag nativeTag = entity.getNbtData();
            if (nativeTag != null) {
                net.minecraft.nbt.CompoundTag tag = NBTConverter.toNative(entity.getNbtData());
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.remove(name);
                }
                createdEntity.fromTag(tag);
            }

            createdEntity.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            world.spawnEntity(createdEntity);
            return new CarpetEntity(createdEntity);
        } else {
            return null;
        }
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    @SuppressWarnings("serial")
    private static class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

}