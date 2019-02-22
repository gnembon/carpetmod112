package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.UnloadOrder;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class CommandUnload extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getUsage(ICommandSender sender)
    {
        return "Usage: unload <brief|verbose|order> <X1> <Y1> <Z1> [<x2> <y2> <z2>]";
    }

    public String getName()
    {
        return "unload";
    }


    public void print_multi_message(List<String> messages, ICommandSender sender)
    {
        for (String line: messages)
        {
            notifyCommandListener(sender, this, line);
        }
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandUnload", sender)) return;
        if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 4 && args.length != 7)
        {
            throw new WrongUsageException(getUsage(sender));
        }
        BlockPos pos = sender.getPosition();
        BlockPos pos2 = null;
        boolean verbose = false;
        boolean order = false;
        boolean custom_dim = false;
        int custom_dim_id = 0;
        if (args.length >0)
        {
            verbose = "verbose".equalsIgnoreCase(args[0]);
        }
        if (args.length >0)
        {
            order = "order".equalsIgnoreCase(args[0]);
        }

        if (args.length >= 4)
        {
            pos = parseBlockPos(sender, args, 1, false);
        }
        if (args.length >= 7)
        {
            pos2 = parseBlockPos(sender, args, 4, false);
        }
        if (args.length > 0)
        {
            if ("overworld".equalsIgnoreCase(args[0]))
            {
                custom_dim = true;
                custom_dim_id = 0;
            }
            if ("nether".equalsIgnoreCase(args[0]))
            {
                custom_dim = true;
                custom_dim_id = -1;
            }
            if ("end".equalsIgnoreCase(args[0]))
            {
                custom_dim = true;
                custom_dim_id = 1;
            }
            if (custom_dim && args.length > 1)
            {
                if ("verbose".equalsIgnoreCase(args[1]))
                {
                    verbose = true;
                }
            }
        }

        if (order)
        {
            List<String> orders = UnloadOrder.check_unload_order((WorldServer)sender.getEntityWorld(), pos, pos2);
            print_multi_message(orders, sender);
            return;
        }
        WorldServer world = (WorldServer) (custom_dim?server.getWorld(custom_dim_id):sender.getEntityWorld() );
        notifyCommandListener(sender, this, "Chunk unloading report for "+world.provider.getDimensionType());
        List<String> report = UnloadOrder.test_save_chunks(world, pos, verbose);
        print_multi_message(report, sender);
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandUnload)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "verbose", "brief", "order", "nether", "overworld", "end");
        }
        if (args.length == 2 && ( "nether".equalsIgnoreCase(args[0]) || "overworld".equalsIgnoreCase(args[0]) || "end".equalsIgnoreCase(args[0]) ))
        {
            return getListOfStringsMatchingLastWord(args, "verbose");
        }
        if (args.length > 1 && args.length <= 4)
        {
            return getTabCompletionCoordinate(args, 1, pos);
        }
        if (args.length > 4 && args.length <= 7)
        {
            return getTabCompletionCoordinate(args, 4, pos);
        }
        return Collections.<String>emptyList();
    }
}
