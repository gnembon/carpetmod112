package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.BlockInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandBlockInfo extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */

    public String getUsage(ICommandSender sender)
    {
        return "Usage: blockinfo <X> <Y> <Z>";
    }

    public String getName()
    {
        return "blockinfo";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandBlockInfo", sender)) return;

        if (args.length != 3)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        BlockPos blockpos = parseBlockPos(sender, args, 0, false);
        World world = sender.getEntityWorld();
        msg(sender, BlockInfo.blockInfo(blockpos, world));
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandBlockInfo)
        {
            return Collections.<String>emptyList();
        }
        if (args.length > 0 && args.length <= 3)
        {
            return getTabCompletionCoordinate(args, 0, pos);
        }
        return Collections.<String>emptyList();
    }
}
