package carpet.commands;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandChunk extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */

    public String getUsage(ICommandSender sender)
    {
        return "Usage: chunk <load | info | unload> <X> <Z>";
    }

    public String getName()
    {
        return "chunk";
    }

    protected World world;
    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandChunk", sender)) return;

        if (args.length != 3) {
            throw new WrongUsageException(getUsage(sender));
        }

        world = sender.getEntityWorld();
        try {
            int chunkX = parseChunkPosition(args[1], sender.getPosition().getX());
            int chunkZ = parseChunkPosition(args[2], sender.getPosition().getZ());

            switch (args[0]){
                case "load":
                    world.getChunk(chunkX, chunkZ);
                    sender.sendMessage(new TextComponentString("Chunk " + chunkX + ", " + chunkZ + " loaded"));
                    return;
                case "unload":
                    unload(sender, chunkX, chunkZ);
                    return;
                case "info":
                default:
                    info(sender, chunkX, chunkZ);

            }
        }catch (Exception e){
            throw new WrongUsageException(getUsage(sender));
        }
    }

    protected void info(ICommandSender sender, int x, int z) throws NoSuchFieldException, IllegalAccessException {
        if(!world.isChunkLoaded(x, z, false)) {
            sender.sendMessage(new TextComponentString(("Chunk is not loaded")));
        }

        long i = ChunkPos.asLong(x, z);
        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        int mask = CommandLoadedChunks.getMask((Long2ObjectOpenHashMap<Chunk>) provider.loadedChunks);
        long key = HashCommon.mix(i) & mask;
        sender.sendMessage(new TextComponentString(("Chunk ideal key is " + key)));
        if (world.isSpawnChunk(x, z))
            sender.sendMessage(new TextComponentString(("Spawn Chunk")));
    }

    protected void unload(ICommandSender sender, int x, int z){
        if(!world.isChunkLoaded(x, z, false)) {
            sender.sendMessage(new TextComponentString(("Chunk is not loaded")));
            return;
        }
        Chunk chunk = world.getChunk(x, z);
        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        provider.queueUnload(chunk);
        sender.sendMessage(new TextComponentString(("Chunk is queue to unload")));
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        int chunkX = sender.getPosition().getX() >> 4;
        int chunkZ = sender.getPosition().getZ() >> 4;

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "info", "load", "unload");
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, Integer.toString(chunkX), "~");
        } else if (args.length == 3) {
            return getListOfStringsMatchingLastWord(args, Integer.toString(chunkZ), "~");
        } else {
            return Collections.emptyList();
        }
    }
}
