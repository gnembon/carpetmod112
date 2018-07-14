package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandLog extends CommandCarpetBase {

    private final String USAGE = "/log (interactive menu) OR /log <logName> <?option> OR /log clear";

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
        if (!(sender instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)sender;

        if (args.length == 0) {
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
        }
        else
        {
            // toggle to default
            if ("clear".equalsIgnoreCase(args[0]))
            {
                LoggerRegistry.getLoggerNames().forEach(logname -> LoggerRegistry.unsubscribePlayer(player.getName(), logname));
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
                boolean subscribed = true;
                if (option == null)
                {
                    subscribed = LoggerRegistry.togglePlayerSubscription(player.getName(), logger.getLogName());
                }
                else
                {
                    LoggerRegistry.subscribePlayer(player.getName(), logger.getLogName(), option);
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
            Logger logger = LoggerRegistry.getLogger(args[0]);
            if (logger != null)
            {
                String [] options = logger.getOptions();
                if (options != null)
                {
                    return getListOfStringsMatchingLastWord(args, options);
                }
            }
        }
        return Collections.<String>emptyList();
    }
}
