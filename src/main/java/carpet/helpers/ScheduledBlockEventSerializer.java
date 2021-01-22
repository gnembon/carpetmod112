package carpet.helpers;

import carpet.CarpetSettings;
import carpet.mixin.accessors.ServerWorldAccessor;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.PersistentState;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ScheduledBlockEventSerializer extends PersistentState {
    private final ArrayList<BlockAction> list = new ArrayList<>();
    private ServerWorld world;

    public ScheduledBlockEventSerializer() {
        this("blockEvents");
    }

    public ScheduledBlockEventSerializer(String name) {
        super(name);
    }

    public void fromTag(CompoundTag nbt) {
        ListTag nbttaglist = nbt.getList("blockEvents", 10);
        for (int i = 0; i < nbttaglist.size(); ++i) {
            CompoundTag nbttagcompound = nbttaglist.getCompound(i);
            BlockAction blockeventdata = new BlockAction(new BlockPos(nbttagcompound.getInt("X"), nbttagcompound.getInt("Y"), nbttagcompound.getInt("Z")), Block.getBlockFromRawId(nbttagcompound.getInt("B") & 4095), nbttagcompound.getInt("ID"), nbttagcompound.getInt("P"));
            list.add(blockeventdata);
        }
    }

    public CompoundTag toTag(CompoundTag compound) {
        ListTag nbttaglist = new ListTag();
        if(CarpetSettings.blockEventSerializer) {
            for (BlockAction blockeventdata : getBlockEventQueue(world)) {
                CompoundTag nbttagcompound = new CompoundTag();
                nbttagcompound.putInt("X", blockeventdata.getPos().getX());
                nbttagcompound.putInt("Y", blockeventdata.getPos().getY());
                nbttagcompound.putInt("Z", blockeventdata.getPos().getZ());
                nbttagcompound.putInt("B", Block.getId(blockeventdata.getBlock()) & 4095);
                nbttagcompound.putInt("ID", blockeventdata.getType());
                nbttagcompound.putInt("P", blockeventdata.getData());
                nbttaglist.add(nbttagcompound);
            }
        }
        compound.put("blockEvents", nbttaglist);
        return compound;
    }

    public void setBlockEvents(ServerWorld world) {
        this.world = world;
        getBlockEventQueue(world).addAll(list);
    }

    private static ArrayList<BlockAction> getBlockEventQueue(ServerWorld world) {
        return BlockEventQueueGetter.getBlockEventQueue(world)[((ServerWorldAccessor) world).getBlockEventCacheIndex()];
    }

    /**
     * WorldServer.ServerBlockEventList is package private and I don't know of a way to get it with mixins so we have
     * to find it with reflection. Inner class to lazy-initialize it.
     */
    private static final class BlockEventQueueGetter {
        static MethodHandle handle = getMethodHandle();

        static ArrayList<BlockAction>[] getBlockEventQueue(ServerWorld world) {
            try {
                //noinspection unchecked
                return (ArrayList<BlockAction>[]) handle.invoke(world);
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
            Class<?> worldServerCls = ServerWorld.class;
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
