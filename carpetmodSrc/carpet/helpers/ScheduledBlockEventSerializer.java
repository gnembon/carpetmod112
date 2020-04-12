package carpet.helpers;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;

public class ScheduledBlockEventSerializer extends WorldSavedData {

    private ArrayList<BlockEventData> list = new ArrayList<>();
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
            for (BlockEventData blockeventdata : world.getBlockEventQueue()) {
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
        world.setBlockEventQueue(list);
    }
}
