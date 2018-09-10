package carpet.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import carpet.CarpetSettings;
import carpet.logging.LogHandler;
import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandLog extends CommandCarpetBase {

    private final String USAGE = "/log (interactive menu) OR /log <logName> [?option] [player] [handler ...] OR /log <logName> clear [player]";

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandLog", sender)) return;
        EntityPlayer player = null;
        if (sender instanceof EntityPlayer)
        {
            player = (EntityPlayer)sender;
        }

        if (args.length == 0)
        {
            if (player == null)
            {
                return;
            }
            Map<String,String> subs = LoggerRegistry.getPlayerSubscriptions(player.getName());
            if (subs == null)
            {
                subs = new HashMap<>();
            }
            List<String> all_logs = new ArrayList<>(LoggerRegistry.getLoggerNames());
            Collections.sort(all_logs);
            Messenger.m(player, "w _____________________");
            Messenger.m(player, "w Available logging options:");
            for (String lname: all_logs)
            {
                List<Object> comp = new ArrayList<>();
                String color = subs.containsKey(lname)?"w":"g";
                comp.add("w  - "+lname+": ");
                Logger logger = LoggerRegistry.getLogger(lname);
                String [] options = logger.getOptions();
                if (options == null)
                {
                    if (subs.containsKey(lname))
                    {
                        comp.add("l Subscribed ");
                    }
                    else
                    {
                        comp.add(color + " [Subscribe] ");
                        comp.add("^w subscribe to " + lname);
                        comp.add("!/log " + lname);
                    }
                }
                else
                {
                    for (String option : logger.getOptions())
                    {
                        if (subs.containsKey(lname) && subs.get(lname).equalsIgnoreCase(option))
                        {
                            comp.add("l [" + option + "] ");
                        } else
                        {
                            comp.add(color + " [" + option + "] ");
                            comp.add("^w subscribe to " + lname + " " + option);
                            comp.add("!/log " + lname + " " + option);
                        }

                    }
                }
                if (subs.containsKey(lname))
                {
                    comp.add("nb [X]");
                    comp.add("^w Click to unsubscribe");
                    comp.add("!/log "+lname);
                }
                Messenger.m(player,comp.toArray(new Object[0]));
            }
            return;
        }
        // toggle to default
        if ("clear".equalsIgnoreCase(args[0]))
        {
            if (args.length > 1)
            {
                player = server.getPlayerList().getPlayerByUsername(args[1]);
            }
            if (player == null)
            {
                throw new WrongUsageException("No player specified");
            }
            for (String logname : LoggerRegistry.getLoggerNames())
            {
                LoggerRegistry.unsubscribePlayer(player.getName(), logname);
            }
            notifyCommandListener(sender, this, "Unsubscribed from all logs");
            return;
        }
        Logger logger = LoggerRegistry.getLogger(args[0]);
        if (logger != null)
        {
            String option = null;
            if (args.length >= 2)
            {
                option = logger.getAcceptedOption(args[1]);
            }
            if (args.length >= 3)
            {
                player = server.getPlayerList().getPlayerByUsername(args[2]);
            }
            if (player == null)
            {
                throw new WrongUsageException("No player specified");
            }
            LogHandler handler = null;
            if (args.length >= 4)
            {
                handler = LogHandler.createHandler(args[3], ArrayUtils.subarray(args, 4, args.length));
                if (handler == null)
                {
                    throw new CommandException("Invalid handler");
                }
            }
            boolean subscribed = true;
            if (args.length >= 2 && "clear".equalsIgnoreCase(args[1]))
            {
                LoggerRegistry.unsubscribePlayer(player.getName(), logger.getLogName());
                subscribed = false;
            }
            else if (option == null)
            {
                subscribed = LoggerRegistry.togglePlayerSubscription(player.getName(), logger.getLogName(), handler);
            }
            else
            {
                LoggerRegistry.subscribePlayer(player.getName(), logger.getLogName(), option, handler);
            }
            if (subscribed)
            {
                Messenger.m(player, "gi Subscribed to " + logger.getLogName() + ".");
            }
            else
            {
                Messenger.m(player, "gi Unsubscribed from " + logger.getLogName() + ".");
            }
        }
        else
        {
            throw new WrongUsageException("No logger named " + args[0] + ".");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        if (!CarpetSettings.getBool("commandLog"))
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            Set<String> options = new HashSet<>(LoggerRegistry.getLoggerNames());
            options.add("clear");
            return getListOfStringsMatchingLastWord(args, options);
        }
        else if (args.length == 2)
        {
            if ("clear".equalsIgnoreCase(args[0]))
            {
                List<String> players = Arrays.asList(server.getOnlinePlayerNames());
                return getListOfStringsMatchingLastWord(args, players.toArray(new String[0]));
            }
            Logger logger = LoggerRegistry.getLogger(args[0]);
            if (logger != null)
            {
                String [] opts = logger.getOptions();
                List<String> options = new ArrayList<>();
                options.add("clear");
                if (opts != null)
                    options.addAll(Arrays.asList(opts));
                else
                    options.add("on");
                return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
            }
        }
        else if (args.length == 3)
        {
            List<String> players = Arrays.asList(server.getOnlinePlayerNames());
            return getListOfStringsMatchingLastWord(args, players.toArray(new String[0]));
        }
        else if (args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, LogHandler.getHandlerNames());
        }

        return Collections.<String>emptyList();
    }
}
