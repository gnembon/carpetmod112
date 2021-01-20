package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;

import javax.annotation.Nullable;

import com.sk89q.worldedit.Vector;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Utility methods for setting tile entities in the world.
 */
final class TileEntityUtils {

    private TileEntityUtils() {
    }

    /**
     * Update the given tag compound with position information.
     *
     * @param tag the tag
     * @param position the position
     * @return a tag compound
     */
    private static CompoundTag updateForSet(CompoundTag tag, Vector position) {
        checkNotNull(tag);
        checkNotNull(position);

        tag.put("x", new IntTag(position.getBlockX()));
        tag.put("y", new IntTag(position.getBlockY()));
        tag.put("z", new IntTag(position.getBlockZ()));

        return tag;
    }

    /**
     * Set a tile entity at the given location.
     *
     * @param world the world
     * @param position the position
     * @param clazz the tile entity class
     * @param tag the tag for the tile entity (may be null to not set NBT data)
     */
    static void setTileEntity(World world, Vector position, Class<? extends BlockEntity> clazz, @Nullable CompoundTag tag) {
        checkNotNull(world);
        checkNotNull(position);
        checkNotNull(clazz);

        BlockEntity tileEntity = constructTileEntity(world, position, clazz);

        if (tileEntity == null) {
            return;
        }

        if (tag != null) {
            // Set X, Y, Z
            updateForSet(tag, position);
            tileEntity.fromTag(tag);
        }

        world.setBlockEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()), tileEntity);
    }

    /**
     * Set a tile entity at the given location using the tile entity ID from
     * the tag.
     *
     * @param world the world
     * @param position the position
     * @param tag the tag for the tile entity (may be null to do nothing)
     */
    static void setTileEntity(World world, Vector position, @Nullable CompoundTag tag) {
        if (tag != null) {
            updateForSet(tag, position);
            BlockEntity tileEntity = BlockEntity.method_26922(world, tag);
            if (tileEntity != null) {
                world.setBlockEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()), tileEntity);
            }
        }
    }

    /**
     * Construct a tile entity from the given class.
     *
     * @param world the world
     * @param position the position
     * @param clazz the class
     * @return a tile entity (may be null if it failed)
     */
    @Nullable
    static BlockEntity constructTileEntity(World world, Vector position, Class<? extends BlockEntity> clazz) {
        Constructor<? extends BlockEntity> baseConstructor;
        try {
            baseConstructor = clazz.getConstructor(); // creates "blank" TE
        } catch (Throwable e) {
            return null; // every TE *should* have this constructor, so this isn't necessary
        }

        BlockEntity genericTE;
        try {
            // Downcast here for return while retaining the type
            genericTE = (BlockEntity) baseConstructor.newInstance();
        } catch (Throwable e) {
            return null;
        }

        /*
        genericTE.blockType = Block.blocksList[block.getId()];
        genericTE.blockMetadata = block.getData();
        genericTE.xCoord = pt.getBlockX();
        genericTE.yCoord = pt.getBlockY();
        genericTE.zCoord = pt.getBlockZ();
        genericTE.worldObj = world;
        */ // handled by internal code

        return genericTE;
    }


}
