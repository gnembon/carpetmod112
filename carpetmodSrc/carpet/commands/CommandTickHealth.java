package carpet.commands;

import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.CarpetProfiler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandTickHealth extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "tickhealth";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "Usage: /tickheath <entities>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandTick", sender)) return;
        if (args.length > 0 && "entities".equalsIgnoreCase(args[0]))
        {
            CarpetProfiler.prepare_entity_report(100);
        }
        else
        {
            CarpetProfiler.prepare_tick_report(100);
        }

    }
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.getBool("commandTick"))
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "entities");
        }
        return Collections.<String>emptyList();
    }
}
