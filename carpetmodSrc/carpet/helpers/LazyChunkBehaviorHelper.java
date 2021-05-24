package carpet.helpers;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static carpet.CarpetSettings.commandLazyChunkBehavior;


public class LazyChunkBehaviorHelper {
    private static final ArrayList<Chunk> lazyProcessingChunks = new ArrayList<Chunk>();

    public static void listLazyChunks(ICommandSender sender){
        if(!lazyProcessingChunks.isEmpty()) {
            for (Chunk chunk : lazyProcessingChunks) {
                TextComponentString text = new TextComponentString("Chunk " + chunk.x + ", " + chunk.z + " in world " + chunk.getWorld().provider.getDimensionType());
                text.getStyle().setColor(TextFormatting.GREEN);
                sender.sendMessage(text);
            }

        }
        else{
            TextComponentString text = new TextComponentString("No chunks to list." );
            text.getStyle().setColor(TextFormatting.GREEN);
            sender.sendMessage(text);
        }
    }
    public static void removeAll(){
        lazyProcessingChunks.clear();
    }

    public static boolean addLazyChunk(Chunk chunk){
        if(!containsLazyChunk(chunk)) {
            lazyProcessingChunks.add(chunk);
            return true;
        }
        return false;
    }
    public static boolean removeLazyChunk(Chunk chunk){
        if(containsLazyChunk(chunk)) {
            lazyProcessingChunks.remove(chunk);
            return true;
        }
        return false;
    }
    public static boolean containsLazyChunk(Chunk chunk){
       if(lazyProcessingChunks.isEmpty())
           return false;
       return lazyProcessingChunks.contains(chunk);
    }

    public static boolean shouldUpdate(Entity entityIn) {
        World world = entityIn.getEntityWorld();
        return shouldUpdate(world, entityIn.getPosition());
    }

    public static boolean shouldUpdate(World worldIn, BlockPos pos) {
        return shouldUpdate(worldIn, new ChunkPos(pos.getX()>> 4,pos.getZ()>> 4));
    }

    public static boolean shouldUpdate(World worldIn, ChunkPos pos) {
        Chunk chunk = worldIn.getChunk(pos.x, pos.z);
        return !containsLazyChunk(chunk);
    }
}
