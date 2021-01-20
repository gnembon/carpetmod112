package carpet.helpers;
//Author: masa

import java.util.List;

import javax.annotation.Nullable;

import carpet.mixin.accessors.WorldAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.WorldChunk;

public class CollisionBoxesOptimizations
{
    public static boolean optimizedGetCollisionBoxes(World world, @Nullable Entity entityIn, Box aabb, boolean p_191504_3_, @Nullable List<Box> outList)
    {
        final int startX = MathHelper.floor(aabb.x1) - 1;
        final int endX = MathHelper.ceil(aabb.x2) + 1;
        final int startY = MathHelper.floor(aabb.y1) - 1;
        final int endY = MathHelper.ceil(aabb.y2) + 1;
        final int startZ = MathHelper.floor(aabb.z1) - 1;
        final int endZ = MathHelper.ceil(aabb.z2) + 1;
        WorldBorder worldborder = world.getWorldBorder();
        boolean flag = entityIn != null && entityIn.isOutsideBorder();
        boolean flag1 = entityIn != null && world.method_26126(entityIn);
        BlockState stateStone = Blocks.STONE.getDefaultState();
        BlockPos.PooledMutable posMutable = BlockPos.PooledMutable.get();

        try
        {
            final int chunkStartX = (startX >> 4);
            final int chunkStartZ = (startZ >> 4);
            final int chunkEndX = (endX >> 4);
            final int chunkEndZ = (endZ >> 4);
            final int yMin = Math.max(0, startY);

            for (int cx = chunkStartX; cx <= chunkEndX; cx++)
            {
                for (int cz = chunkStartZ; cz <= chunkEndZ; cz++)
                {
                    if (((WorldAccessor) world).invokeIsChunkLoaded(cx, cz, false))
                    {
                        WorldChunk chunk = world.method_25975(cx, cz);
                        final int xMin = Math.max(cx << 4, startX);
                        final int zMin = Math.max(cz << 4, startZ);
                        final int xMax = Math.min((cx << 4) + 15, endX - 1);
                        final int zMax = Math.min((cz << 4) + 15, endZ - 1);
                        final int yMax = Math.min(chunk.method_27410() + 15, endY - 1);

                        for (int x = xMin; x <= xMax; ++x)
                        {
                            for (int z = zMin; z <= zMax; ++z)
                            {
                                boolean xIsEdge = x == startX || x == endX - 1;
                                boolean zIsEdge = z == startZ || z == endZ - 1;

                                if (! xIsEdge || ! zIsEdge)
                                {
                                    for (int y = yMin; y <= yMax; ++y)
                                    {
                                        if (! xIsEdge && ! zIsEdge || y != endY - 1)
                                        {
                                            if (p_191504_3_)
                                            {
                                                if (x < -30000000 || x >= 30000000 || z < -30000000 || z >= 30000000)
                                                {
                                                    return true;
                                                }
                                            }
                                            else if (entityIn != null && flag == flag1)
                                            {
                                                entityIn.setOutsideBorder(! flag1);
                                            }

                                            posMutable.set(x, y, z);
                                            BlockState state;

                                            if (! p_191504_3_ && ! worldborder.contains(posMutable) && flag1)
                                            {
                                                state = stateStone;
                                            }
                                            else
                                            {
                                                state = chunk.getBlockState(posMutable);
                                            }

                                            state.method_27180(world, posMutable.toImmutable(), aabb, outList, entityIn, false);

                                            if (p_191504_3_ && ! outList.isEmpty())
                                            {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            posMutable.free();
        }

        return !outList.isEmpty();
    }
}