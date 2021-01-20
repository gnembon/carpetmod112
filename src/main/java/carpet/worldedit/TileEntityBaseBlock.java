package carpet.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

class TileEntityBaseBlock extends BaseBlock implements TileEntityBlock {

    public TileEntityBaseBlock(int type, int data, BlockEntity tile) {
        super(type, data);
        setNbtData(NBTConverter.fromNative(copyNbtData(tile)));
    }

    private static CompoundTag copyNbtData(BlockEntity tile) {
        CompoundTag tag = new CompoundTag();
        tile.toTag(tag);
        return tag;
    }

}