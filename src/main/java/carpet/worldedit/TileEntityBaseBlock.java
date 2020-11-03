package carpet.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

class TileEntityBaseBlock extends BaseBlock implements TileEntityBlock {

    public TileEntityBaseBlock(int type, int data, TileEntity tile) {
        super(type, data);
        setNbtData(NBTConverter.fromNative(copyNbtData(tile)));
    }

    private static NBTTagCompound copyNbtData(TileEntity tile) {
        NBTTagCompound tag = new NBTTagCompound();
        tile.writeToNBT(tag);
        return tag;
    }

}