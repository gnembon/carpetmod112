package carpet.commands;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import carpet.CarpetSettings;
import carpet.utils.TickingArea;
import net.minecraft.class_6175;
import net.minecraft.class_6178;
import net.minecraft.class_6182;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.World;

public class CommandTickingArea extends CommandCarpetBase
{

    private static final String USAGE = "/tickingarea <add|remove|remove_all|list> ...";
    private static final String USAGE_ADD = "/tickingarea add [square|circle|spawnChunks] ...";
    private static final String USAGE_ADD_SQUARE = "/tickingarea add [square] <fromChunk: x z> <toChunk: x z> [name]";
    private static final String USAGE_ADD_CIRCLE = "/tickingarea add circle <centerChunk: x z> <radius> [name]";
    private static final String USAGE_REMOVE = "/tickingarea remove <name|chunkPos: x z>";
    
    @Override
    public String method_29277()
    {
        return "tickingarea";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
    {
        if (!command_enabled("tickingAreas", sender))
            return;
        
        if (args.length < 1)
            throw new class_6182(USAGE);
        
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
            throw new class_6182(USAGE);
        }
    }
    
    private static ColumnPos parseChunkPos(CommandSource sender, String[] args, int index) throws class_6178
    {
        int x = (int) Math.round(method_28702(sender.getBlockPos().getX() >> 4, args[index], false).method_28750());
        int z = (int) Math.round(method_28702(sender.getBlockPos().getZ() >> 4, args[index + 1], false).method_28750());
        return new ColumnPos(x, z);
    }
    
    private void addTickingArea(CommandSource sender, String[] args) throws class_6175
    {
        if (args.length < 2)
            throw new class_6182(USAGE_ADD);
        
        int index = 2;
        TickingArea area;
        
        if ("circle".equals(args[1]))
        {
            if (args.length < 5)
                throw new class_6182(USAGE_ADD_CIRCLE);
            ColumnPos center = parseChunkPos(sender, args, index);
            index += 2;
            double radius = method_28716(args[index++], 0);
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
                throw new class_6182(USAGE_ADD_SQUARE);
            ColumnPos from = parseChunkPos(sender, args, index);
            index += 2;
            ColumnPos to = parseChunkPos(sender, args, index);
            index += 2;
            ColumnPos min = new ColumnPos(Math.min(from.x, to.x), Math.min(from.z, to.z));
            ColumnPos max = new ColumnPos(Math.max(from.x, to.x), Math.max(from.z, to.z));
            area = new TickingArea.Square(min, max);
        }
        
        if (args.length > index)
        {
            area.setName(method_28729(args, index));
        }
        
        TickingArea.addTickingArea(sender.getEntityWorld(), area);
        
        for (ColumnPos chunk : area.listIncludedChunks(sender.getEntityWorld()))
        {
            // Load chunk
            sender.getEntityWorld().method_25975(chunk.x, chunk.z);
        }
        
        method_28710(sender, this, "Added ticking area");
    }
    
    private void removeTickingArea(CommandSource sender, String[] args) throws class_6175
    {
        if (args.length < 2)
            throw new class_6182(USAGE_REMOVE);
        
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
                ColumnPos pos = parseChunkPos(sender, args, 1);
                removed = TickingArea.removeTickingAreas(sender.getEntityWorld(), pos.x, pos.z);
            }
            catch (class_6175 e)
            {
                byName = true;
            }
        }
        if (byName)
        {
            removed = TickingArea.removeTickingAreas(sender.getEntityWorld(), method_28729(args, 1));
        }
        
        if (removed)
            method_28710(sender, this, "Removed ticking area");
        else
            throw new class_6175("Couldn't remove ticking area");
    }
    
    private void removeAllTickingAreas(CommandSource sender, String[] args) throws class_6175
    {
        TickingArea.removeAllTickingAreas(sender.getEntityWorld());
        method_28710(sender, this, "Removed all ticking areas");
    }
    
    private void listTickingAreas(CommandSource sender, String[] args) throws class_6175
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
    
    private void listAreas(CommandSource sender, World world)
    {
        if (world.dimension.hasVisibleSky() && !CarpetSettings.disableSpawnChunks)
            sender.sendSystemMessage(new LiteralText("Spawn chunks are enabled"));
        
        sender.sendSystemMessage(new LiteralText("Ticking areas in " + world.dimension.getType().method_27531() + ":"));
        
        for (TickingArea area : TickingArea.getTickingAreas(world))
        {
            String msg = "- ";
            if (area.getName() != null)
                msg += area.getName() + ": ";
            
            msg += area.format();
            
            sender.sendSystemMessage(new LiteralText(msg));
        }
    }
    
    private static List<String> tabCompleteChunkPos(CommandSource sender, BlockPos targetPos, String[] args, int index)
    {
        if (targetPos == null)
        {
            return Lists.newArrayList("~");
        }
        else
        {
            if (index == args.length)
            {
                int x = sender.getBlockPos().getX() / 16;
                return Lists.newArrayList(String.valueOf(x));
            }
            else
            {
                int z = sender.getBlockPos().getZ() / 16;
                return Lists.newArrayList(String.valueOf(z));
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args,
            BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return method_28732(args, "add", "remove", "remove_all", "list");
        }
        else if ("add".equals(args[0]))
        {
            if (args.length == 2)
            {
                List<String> completions = tabCompleteChunkPos(sender, targetPos, args, 2);
                Collections.addAll(completions, "square", "circle", "spawnChunks");
                return method_28731(args, completions);
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
                return method_28731(args, completions);
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
                return method_28732(args, "all-dimensions");
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
