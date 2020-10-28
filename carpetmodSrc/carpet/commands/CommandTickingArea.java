package carpet.commands;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import carpet.CarpetSettings;
import carpet.utils.TickingArea;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandTickingArea extends CommandCarpetBase
{

    private static final String USAGE = "/tickingarea <add|remove|remove_all|list> ...";
    private static final String USAGE_ADD = "/tickingarea add [square|circle|spawnChunks] ...";
    private static final String USAGE_ADD_SQUARE = "/tickingarea add [square] <fromChunk: x z> <toChunk: x z> [name]";
    private static final String USAGE_ADD_CIRCLE = "/tickingarea add circle <centerChunk: x z> <radius> [name]";
    private static final String USAGE_REMOVE = "/tickingarea remove <name|chunkPos: x z>";
    
    @Override
    public String getName()
    {
        return "tickingarea";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("tickingAreas", sender))
            return;
        
        if (args.length < 1)
            throw new WrongUsageException(USAGE);
        
        switch (args[0])
        {
        case "add":
            addTickingArea(sender, args);
            break;
        case "remove":
            removeTickingArea(sender, args);
            break;
        case "remove_all":
            removeAllTickingAreas(sender, args);
            break;
        case "list":
            listTickingAreas(sender, args);
            break;
        default:
            throw new WrongUsageException(USAGE);
        }
    }
    
    private static ChunkPos parseChunkPos(ICommandSender sender, String[] args, int index) throws CommandException
    {
        int x = (int) Math.round(parseCoordinate(sender.getPosition().getX() >> 4, args[index], false).getResult());
        int z = (int) Math.round(parseCoordinate(sender.getPosition().getZ() >> 4, args[index + 1], false).getResult());
        return new ChunkPos(x, z);
    }
    
    private void addTickingArea(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw new WrongUsageException(USAGE_ADD);
        
        int index = 2;
        TickingArea area;
        
        if ("circle".equals(args[1]))
        {
            if (args.length < 5)
                throw new WrongUsageException(USAGE_ADD_CIRCLE);
            ChunkPos center = parseChunkPos(sender, args, index);
            index += 2;
            double radius = parseDouble(args[index++], 0);
            area = new TickingArea.Circle(center, radius);
        }
        else if ("spawnChunks".equals(args[1]))
        {
            area = new TickingArea.SpawnChunks();
        }
        else
        {
            if (!"square".equals(args[1]))
                index = 1;
            if (args.length < index + 4)
                throw new WrongUsageException(USAGE_ADD_SQUARE);
            ChunkPos from = parseChunkPos(sender, args, index);
            index += 2;
            ChunkPos to = parseChunkPos(sender, args, index);
            index += 2;
            ChunkPos min = new ChunkPos(Math.min(from.x, to.x), Math.min(from.z, to.z));
            ChunkPos max = new ChunkPos(Math.max(from.x, to.x), Math.max(from.z, to.z));
            area = new TickingArea.Square(min, max);
        }
        
        if (args.length > index)
        {
            area.setName(buildString(args, index));
        }
        
        TickingArea.addTickingArea(sender.getEntityWorld(), area);
        
        for (ChunkPos chunk : area.listIncludedChunks(sender.getEntityWorld()))
        {
            // Load chunk
            sender.getEntityWorld().getChunk(chunk.x, chunk.z);
        }
        
        notifyCommandListener(sender, this, "Added ticking area");
    }
    
    private void removeTickingArea(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw new WrongUsageException(USAGE_REMOVE);
        
        boolean byName = false;
        boolean removed = false;
        if (args.length < 3)
        {
            byName = true;
        }
        else
        {
            try
            {
                ChunkPos pos = parseChunkPos(sender, args, 1);
                removed = TickingArea.removeTickingAreas(sender.getEntityWorld(), pos.x, pos.z);
            }
            catch (NumberInvalidException e)
            {
                byName = true;
            }
        }
        if (byName)
        {
            removed = TickingArea.removeTickingAreas(sender.getEntityWorld(), buildString(args, 1));
        }
        
        if (removed)
            notifyCommandListener(sender, this, "Removed ticking area");
        else
            throw new CommandException("Couldn't remove ticking area");
    }
    
    private void removeAllTickingAreas(ICommandSender sender, String[] args) throws CommandException
    {
        TickingArea.removeAllTickingAreas(sender.getEntityWorld());
        notifyCommandListener(sender, this, "Removed all ticking areas");
    }
    
    private void listTickingAreas(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 1 && "all-dimensions".equals(args[1]))
        {
            for (World world : sender.getServer().worlds)
            {
                listAreas(sender, world);
            }
        }
        else
        {
            listAreas(sender, sender.getEntityWorld());
        }
    }
    
    private void listAreas(ICommandSender sender, World world)
    {
        if (world.provider.isSurfaceWorld() && !CarpetSettings.disableSpawnChunks)
            sender.sendMessage(new TextComponentString("Spawn chunks are enabled"));
        
        sender.sendMessage(new TextComponentString("Ticking areas in " + world.provider.getDimensionType().getName() + ":"));
        
        for (TickingArea area : TickingArea.getTickingAreas(world))
        {
            String msg = "- ";
            if (area.getName() != null)
                msg += area.getName() + ": ";
            
            msg += area.format();
            
            sender.sendMessage(new TextComponentString(msg));
        }
    }
    
    private static List<String> tabCompleteChunkPos(ICommandSender sender, BlockPos targetPos, String[] args, int index)
    {
        if (targetPos == null)
        {
            return Lists.newArrayList("~");
        }
        else
        {
            if (index == args.length)
            {
                int x = sender.getPosition().getX() / 16;
                return Lists.newArrayList(String.valueOf(x));
            }
            else
            {
                int z = sender.getPosition().getZ() / 16;
                return Lists.newArrayList(String.valueOf(z));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "add", "remove", "remove_all", "list");
        }
        else if ("add".equals(args[0]))
        {
            if (args.length == 2)
            {
                List<String> completions = tabCompleteChunkPos(sender, targetPos, args, 2);
                Collections.addAll(completions, "square", "circle", "spawnChunks");
                return getListOfStringsMatchingLastWord(args, completions);
            }
            int index = "square".equals(args[1]) || "circle".equals(args[1]) ? 3 : 2;
            if (args.length >= index && args.length < index + 2)
            {
                return tabCompleteChunkPos(sender, targetPos, args, index);
            }
            else if (args.length >= index + 2 && args.length < index + 4)
            {
                return tabCompleteChunkPos(sender, targetPos, args, index + 2);
            }
            else
            {
                return Collections.emptyList();
            }
        }
        else if ("remove".equals(args[0]))
        {
            if (args.length == 2)
            {
                List<String> completions = tabCompleteChunkPos(sender, targetPos, args, 2);
                TickingArea.getTickingAreas(sender.getEntityWorld()).stream().filter(area -> area.getName() != null)
                    .forEach(area -> completions.add(area.getName()));
                return getListOfStringsMatchingLastWord(args, completions);
            }
            else if (args.length == 3)
            {
                return tabCompleteChunkPos(sender, targetPos, args, 2);
            }
            else
            {
                return Collections.emptyList();
            }
        }
        else if ("list".equals(args[0]))
        {
            if (args.length == 2)
            {
                return getListOfStringsMatchingLastWord(args, "all-dimensions");
            }
            else
            {
                return Collections.emptyList();
            }
        }
        else
        {
            return Collections.emptyList();
        }
    }

}
