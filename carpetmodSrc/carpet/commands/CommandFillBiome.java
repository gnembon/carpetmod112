package carpet.commands;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class CommandFillBiome extends CommandCarpetBase
{

    @Override
    public String getName()
    {
        return "fillbiome";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/fillbiome <from: x z> <to: x z> <biome>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandFillBiome", sender))
            return;
        
        if (args.length < 5)
            throw new WrongUsageException(getUsage(sender));
        
        int x1 = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[0], false).getResult());
        int z1 = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[1], false).getResult());
        int x2 = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[2], false).getResult());
        int z2 = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[3], false).getResult());
        
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        
        Biome biome;
        try
        {
            biome = Biome.getBiomeForId(Integer.parseInt(args[4]));
        }
        catch (NumberFormatException e)
        {
            biome = Biome.REGISTRY.getObject(new ResourceLocation(args[4]));
        }
        if (biome == null)
        {
            throw new CommandException("Unknown biome " + args[4]);
        }
        byte biomeId = (byte) (Biome.getIdForBiome(biome) & 255);
        
        WorldServer world = (WorldServer) sender.getEntityWorld();
        if (!world.isAreaLoaded(new BlockPos(minX, 0, minZ), new BlockPos(maxX, 0, maxZ)))
        {
            throw new CommandException("commands.fill.outOfWorld");
        }
        
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        
        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                Chunk chunk = world.getChunk(pos.setPos(x, 0, z));
                chunk.getBiomeArray()[(x & 15) | (z & 15) << 4] = biomeId;
                chunk.markDirty();
            }
        }
        
        int minChunkX = Math.floorDiv(minX, 16);
        int maxChunkX = Math.floorDiv(maxX, 16);
        int minChunkZ = Math.floorDiv(minZ, 16);
        int maxChunkZ = Math.floorDiv(maxZ, 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                PlayerChunkMapEntry entry = world.playerChunkMap.getEntry(chunkX, chunkZ);
                if (entry != null)
                {
                    Chunk chunk = entry.getChunk();
                    if (chunk != null)
                    {
                        SPacketChunkData packet = new SPacketChunkData(chunk, 65535);
                        for (EntityPlayerMP player : entry.players)
                            player.connection.sendPacket(packet);
                    }
                }
            }
        }
        
        notifyCommandListener(sender, this, ((maxX - minX + 1) * (maxZ - minZ + 1)) + " biome blocks changed");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1 || args.length == 3)
        {
            if (targetPos == null)
                return getListOfStringsMatchingLastWord(args, "~");
            else
                return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getX()));
        }
        else if (args.length == 2 || args.length == 4)
        {
            if (targetPos == null)
                return getListOfStringsMatchingLastWord(args, "~");
            else
                return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getZ()));
        }
        else if (args.length == 5)
        {
            return getListOfStringsMatchingLastWord(args, Biome.REGISTRY.getKeys());
        }
        else
        {
            return Collections.emptyList();
        }
    }

}
