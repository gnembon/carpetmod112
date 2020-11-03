package carpet.helpers;

import carpet.CarpetSettings;
import carpet.mixin.accessors.WorldServerAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ScheduledBlockEventSerializer extends WorldSavedData {
    private final ArrayList<BlockEventData> list = new ArrayList<>();
    private WorldServer world;

    public ScheduledBlockEventSerializer() {
        this("blockEvents");
    }

    public ScheduledBlockEventSerializer(String name) {
        super(name);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList nbttaglist = nbt.getTagList("blockEvents", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            BlockEventData blockeventdata = new BlockEventData(new BlockPos(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Y"), nbttagcompound.getInteger("Z")), Block.getBlockById(nbttagcompound.getInteger("B") & 4095), nbttagcompound.getInteger("ID"), nbttagcompound.getInteger("P"));
            list.add(blockeventdata);
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbttaglist = new NBTTagList();
        if(CarpetSettings.blockEventSerializer) {
            for (BlockEventData blockeventdata : getBlockEventQueue(world)) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setInteger("X", blockeventdata.getPosition().getX());
                nbttagcompound.setInteger("Y", blockeventdata.getPosition().getY());
                nbttagcompound.setInteger("Z", blockeventdata.getPosition().getZ());
                nbttagcompound.setInteger("B", Block.getIdFromBlock(blockeventdata.getBlock()) & 4095);
                nbttagcompound.setInteger("ID", blockeventdata.getEventID());
                nbttagcompound.setInteger("P", blockeventdata.getEventParameter());
                nbttaglist.appendTag(nbttagcompound);
            }
        }
        compound.setTag("blockEvents", nbttaglist);
        return compound;
    }

    public void setBlockEvents(WorldServer world) {
        this.world = world;
        getBlockEventQueue(world).addAll(list);
    }

    private static ArrayList<BlockEventData> getBlockEventQueue(WorldServer world) {
        return BlockEventQueueGetter.getBlockEventQueue(world)[((WorldServerAccessor) world).getBlockEventCacheIndex()];
    }

    /**
     * WorldServer.ServerBlockEventList is package private and I don't know of a way to get it with mixins so we have
     * to find it with reflection. Inner class to lazy-initialize it.
     */
    private static final class BlockEventQueueGetter {
        static MethodHandle handle = getMethodHandle();

        static ArrayList<BlockEventData>[] getBlockEventQueue(WorldServer world) {
            try {
                //noinspection unchecked
                return (ArrayList<BlockEventData>[]) handle.invoke(world);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        /**
         * Searches for the {@link MethodHandle} accessing the field {@link WorldServer#blockEventQueue}
         * @return a {@link MethodHandle} of type {@code ()[Lnet/minecraft/world/WorldServer$ServerBlockEventList;}
         */
        private static MethodHandle getMethodHandle() {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> worldServerCls = WorldServer.class;
            for (Field f : worldServerCls.getDeclaredFields()) {
                Class<?> type = f.getType();
                // We're looking for WorldServer.ServerBlockEventList[] which is an array
                if (!type.isArray()) continue;
                Class<?> baseCls = type.getComponentType();
                if (baseCls.getEnclosingClass() != worldServerCls) continue;
                // WorldServer.ServerBlockEventList is the only inner class of WorldServer so this should be the field we want
                try {
                    f.setAccessible(true);
                    return lookup.unreflectGetter(f);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            throw new IllegalStateException("Could not get block event queue field");
        }
    }
}
