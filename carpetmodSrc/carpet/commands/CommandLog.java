package carpet.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import carpet.logging.LoggerOptions;
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

    private final String USAGE = "/log (interactive menu) OR /log <logName> [?option] [player] [handler ...] OR /log <logName> clear [player] OR /log defaults (interactive menu) OR /log setDefault <logName> [?option] [handler ...] OR /log removeDefault <logName>";

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
            Map<String, LoggerOptions> subs = LoggerRegistry.getPlayerSubscriptions(player.getName());
            if (subs == null)
            {
                subs = new HashMap<>();
            }
            List<String> all_logs = Arrays.asList(LoggerRegistry.getLoggerNames(this instanceof CommandDebuglogger ? 1 : 2));
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
                        comp.add("^w toggle subscription to " + lname);
                        comp.add("!/log " + lname);
                    }
                }
                else
                {
                    for (String option : logger.getOptions())
                    {
                        if (subs.containsKey(lname) && subs.get(lname).option.equalsIgnoreCase(option))
                        {
                            comp.add("l [" + option + "] ");
                        } else
                        {
                            comp.add(color + " [" + option + "] ");
                            comp.add("^w toggle subscription to " + lname + " " + option);
                            comp.add("!/log " + lname + " " + option);
                        }

                    }
                }
                if (subs.containsKey(lname))
                {
                    comp.add("nb [X]");
                    comp.add("^w Click to toggle subscription");
                    comp.add("!/log "+lname);
                }
                Messenger.m(player,comp.toArray(new Object[0]));
            }
            return;
        }

        // toggle to default
        if ("reset".equalsIgnoreCase(args[0]))
        {
            if (args.length > 1)
            {
                player = server.getPlayerList().getPlayerByUsername(args[1]);
            }
            if (player == null)
            {
                throw new WrongUsageException("No player specified");
            }
            LoggerRegistry.resetSubscriptions(server, player.getName());
            notifyCommandListener(sender, this, "Unsubscribed from all logs and restored default subscriptions");
            return;
        }

        if ("defaults".equalsIgnoreCase(args[0])) {
            if (player == null)
            {
                return;
            }
            Map<String, LoggerOptions> subs = LoggerRegistry.getDefaultSubscriptions();

            List<String> all_logs = Arrays.asList(LoggerRegistry.getLoggerNames(this instanceof CommandDebuglogger ? 1 : 2));
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
                        comp.add("^w set default subscription to " + lname);
                        comp.add("!/log setDefault " + lname);
                    }
                }
                else
                {
                    for (String option : logger.getOptions())
                    {
                        if (subs.containsKey(lname) && subs.get(lname).option.equalsIgnoreCase(option))
                        {
                            comp.add("l [" + option + "] ");
                        } else
                        {
                            comp.add(color + " [" + option + "] ");
                            comp.add("^w set default subscription to " + lname + " " + option);
                            comp.add("!/log setDefault " + lname + " " + option);
                        }

                    }
                }
                if (subs.containsKey(lname))
                {
                    comp.add("nb [X]");
                    comp.add("^w Click to remove default subscription");
                    comp.add("!/log removeDefault " + lname);
                }
                Messenger.m(player,comp.toArray(new Object[0]));
            }
            return;
        }

        if ("setDefault".equalsIgnoreCase(args[0])) {
            if (args.length >= 2) {
                Logger logger = LoggerRegistry.getLogger(args[1]);
                if (logger != null) {
                    String option = logger.getDefault();
                    if (args.length >= 3) {
                        option = logger.getAcceptedOption(args[2]);
                    }

                    LogHandler handler = null;
                    if (args.length >= 4) {
                        handler = LogHandler.createHandler(args[3], ArrayUtils.subarray(args, 4, args.length));
                        if (handler == null) {
                            throw new CommandException("Invalid handler");
                        }
                    }

                    LoggerRegistry.setDefault(server, args[1], option, handler);
                    Messenger.m(player, "gi Added " + logger.getLogName() + " to default subscriptions.");
                    return;
                } else {
                    throw new WrongUsageException("No logger named " + args[1] + ".");
                }
            } else {
                throw new WrongUsageException("No logger specified.");
            }
        }

        if ("removeDefault".equalsIgnoreCase(args[0])) {
            if (args.length > 1) {
                Logger logger = LoggerRegistry.getLogger(args[1]);
                if (logger != null) {
                    LoggerRegistry.removeDefault(server, args[1]);
                    Messenger.m(player, "gi Removed " + logger.getLogName() + " from default subscriptions.");
                    return;
                } else {
                    throw new WrongUsageException("No logger named " + args[1] + ".");
                }
            } else {
                throw new WrongUsageException("No logger specified.");
            }
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
                LoggerRegistry.unsubscribePlayer(server, player.getName(), logger.getLogName());
                subscribed = false;
            }
            else if (option == null)
            {
                subscribed = LoggerRegistry.togglePlayerSubscription(server, player.getName(), logger.getLogName(), handler);
            }
            else
            {
                LoggerRegistry.subscribePlayer(server, player.getName(), logger.getLogName(), option, handler);
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
        if (!CarpetSettings.commandLog)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            Set<String> options = new HashSet<String>(Arrays.asList(LoggerRegistry.getLoggerNames(this instanceof CommandDebuglogger ? 1 : 2)));
            options.add("clear");
            options.add("defaults");
            options.add("setDefault");
            options.add("removeDefault");
            return getListOfStringsMatchingLastWord(args, options);
        }
        else if (args.length == 2)
        {
            if ("clear".equalsIgnoreCase(args[0]))
            {
                List<String> players = Arrays.asList(server.getOnlinePlayerNames());
                return getListOfStringsMatchingLastWord(args, players.toArray(new String[0]));
            }

            if ("setDefault".equalsIgnoreCase(args[0]) || "removeDefault".equalsIgnoreCase(args[0]))
            {
                Set<String> options = new HashSet<String>(Arrays.asList(LoggerRegistry.getLoggerNames(this instanceof CommandDebuglogger ? 1 : 2)));
                return getListOfStringsMatchingLastWord(args, options);
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
            if ("setDefault".equalsIgnoreCase(args[0]))
            {
                Logger logger = LoggerRegistry.getLogger(args[1]);
                if (logger != null)
                {
                    String [] opts = logger.getOptions();
                    List<String> options = new ArrayList<>();
                    if (opts != null)
                        options.addAll(Arrays.asList(opts));

                    return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
                }
            }

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
