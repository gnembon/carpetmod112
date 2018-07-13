package narcolepticfrog.rsmm;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DimPos {
    private int dim;
    private BlockPos pos;

    public DimPos(World w, BlockPos pos) {
        this(w.provider.getDimensionType().getId(), pos);
    }

    public DimPos(int dim, BlockPos pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public int getDim() {
        return dim;
    }

    public BlockPos getPos() {
        return pos;
    }

    public DimPos offset(EnumFacing direction) {
        return new DimPos(dim, pos.offset(direction));
    }

    @Override
    public String toString() {
        return "DimPos[dim = " + dim +
                ", x = " + pos.getX() +
                ", y = " + pos.getY() +
                ", z = " + pos.getZ() + "]";
    }

    public int hashCode() {
        return Integer.hashCode(dim) ^ pos.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DimPos) {
            DimPos o = (DimPos)obj;
            return o.dim == this.dim && o.pos.equals(this.pos);
        }
        return false;
    }

    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeInt(dim);
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
    }

    public static DimPos readFromBuffer(PacketBuffer buffer) {
        int dim = buffer.readInt();
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        return new DimPos(dim, new BlockPos(x,y,z));
    }
}
