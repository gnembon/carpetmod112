package carpet.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.item.EnumDyeColor;

public class CommandCounter extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getUsage(ICommandSender sender)
    {
        return "Usage: counter <color> <reset/realtime>";
    }

    public String getName()
    {
        return "counter";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("hopperCounters", sender)) return;
        World world = sender.getEntityWorld();
        if (args.length == 0)
        {

            msg(sender, HopperCounter.query_hopper_all_stats(server, false));
            return;
        }
        if ("realtime".equalsIgnoreCase(args[0]))
        {
            msg(sender, HopperCounter.query_hopper_all_stats(server, true));
            return;
        }
        if ("reset".equalsIgnoreCase(args[0]))
        {
            HopperCounter.reset_hopper_counter(world, null);
            notifyCommandListener(sender, this, "All counters restarted.");
            return;
        }
        String color = args[0];
        if (args.length == 1)
        {
            msg(sender, HopperCounter.query_hopper_stats_for_color(server, color, false, false));
            return;
        }
        if ("realtime".equalsIgnoreCase(args[1]))
        {
            msg(sender, HopperCounter.query_hopper_stats_for_color(server, color, true, false));
            return;
        }
        if ("reset".equalsIgnoreCase(args[1]))
        {
            HopperCounter.reset_hopper_counter(world, color);
            notifyCommandListener(sender, this, String.format("%s counters restarted.", color));
            return;
        }
        throw new WrongUsageException(getUsage(sender));

    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.hopperCounters)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            List<String> lst = new ArrayList<String>();
            lst.add("reset");
            for (EnumDyeColor clr : EnumDyeColor.values())
            {
                lst.add(clr.toString());
            }
            lst.add("realtime");
            String[] stockArr = new String[lst.size()];
            stockArr = lst.toArray(stockArr);
            return getListOfStringsMatchingLastWord(args, stockArr);
        }
        if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, "reset", "realtime");
        }
        return Collections.<String>emptyList();
    }
}
