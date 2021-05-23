package carpet.helpers;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static carpet.CarpetSettings.commandLazyChunkBehavior;


public class LazyChunkBehaviorHelper {
    private static final ArrayList<Chunk> lazyProcessingChunks = new ArrayList<Chunk>();

    private static boolean addLazyChunk(Chunk chunk){
        if(!containsLazyChunk(chunk)) {
            lazyProcessingChunks.add(chunk);
            return true;
        }
        return false;
    }
    private static boolean removeLazyChunk(Chunk chunk){
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
    public static String CommandFeedBack(Chunk chunk){
        if (chunk==null){
            return "Failed to add! Chunk is NULL.";
        }
        else if(addLazyChunk(chunk))
            return "Added.";
        else if(removeLazyChunk(chunk))
            return "Removed.";
        else
            return "Not Added or Remove. The command is disabled.";
    }

    public static boolean shouldUpdateEntity(Entity entityIn) {
        World world = entityIn.getEntityWorld();
        Chunk chunk = world.getChunk(entityIn.getPosition().getX() >> 4, entityIn.getPosition().getZ() >> 4);
        return !containsLazyChunk(chunk);
    }
}
