package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.DistanceCalculator;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandDistance extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getUsage(ICommandSender sender)
    {
        return "Usage: distance <X1> <Y1> <Z1> <X2> <Y2> <Z2>";
    }

    public String getName()
    {
        return "distance";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandDistance", sender)) return;
        if (args.length != 6)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        BlockPos blockpos = parseBlockPos(sender, args, 0, false);
        BlockPos blockpos2 = parseBlockPos(sender, args, 3, false);
        msg(sender, DistanceCalculator.print_distance_two_points(blockpos, blockpos2));

    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandDistance)
        {
            return Collections.<String>emptyList();
        }
        if (args.length > 0 && args.length <= 3)
        {
            return getTabCompletionCoordinate(args, 0, pos);
        }
        if (args.length > 3 && args.length <= 6)
        {
            return getTabCompletionCoordinate(args, 3, pos);
        }
        return Collections.<String>emptyList();
    }
}
