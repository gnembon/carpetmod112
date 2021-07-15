package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.lifetime.LifeTimeTracker;
import carpet.helpers.lifetime.utils.SpecificDetailMode;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandLifeTime extends CommandCarpetBase
{
    private static final String NAME = "lifetime";
    private static final LifeTimeTracker tracker = LifeTimeTracker.getInstance();

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/lifetime tracking [<start|stop|restart|realtime>]\n" +
                "/lifetime <entity_type> [<life_time|spawning|removal> [<realtime>]]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandLifeTime", sender))
        {
            return;
        }
        if (args.length == 0)
        {
            Messenger.s(sender, getUsage(sender));
        }
        else
        {
            switch (args[0])
            {
                case "tracking":
                    this.executeTracking(server, sender, args);
                    break;
                case "help":
                    tracker.showHelp(sender);
                    break;
                default:
                    this.executeDisplayResult(server, sender, args);
                    break;
            }
        }
    }

    private void executeTracking(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 1)  // lifetime tracking
        {
            tracker.reportTracking(sender, false);
            return;
        }
        switch (args[1])
        {
            case "start":
                tracker.startTracking(sender, true);
                break;
            case "stop":
                tracker.stopTracking(sender, true);
                break;
            case "restart":
                tracker.restartTracking(sender);
                break;
            case "realtime":
                tracker.reportTracking(sender, true);
                break;
            default:
                throw new WrongUsageException("unknown command: " + args[1]);
        }
    }

    private void executeDisplayResult(MinecraftServer server, ICommandSender sender, String[] args)
    {
        String entityTypeInput = args[0];
        if (args.length == 1)  // lifetime creeper
        {
            tracker.printTrackingResultSpecific(sender, entityTypeInput, null, false);
        }
        else
        {
            if (args[1].equals("realtime"))  // lifetime creeper realtime
            {
                tracker.printTrackingResultSpecific(sender, entityTypeInput, null, true);
            }
            else if (args.length == 2)  // lifetime creeper xxx
            {
                tracker.printTrackingResultSpecific(sender, entityTypeInput, args[1], false);
            }
            else if (args.length == 3 && args[2].equals("realtime"))  // lifetime creeper xxx realtime
            {
                tracker.printTrackingResultSpecific(sender, entityTypeInput, args[1], true);
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandLifeTime)
        {
            return Collections.emptyList();
        }
        Set<String> entityTypes = tracker.getAvailableEntityType().collect(Collectors.toSet());
        if (args.length == 1)
        {
            List<String> suggestions = Lists.newArrayList(entityTypes);
            suggestions.add("tracking");
            suggestions.add("help");
            return getListOfStringsMatchingLastWord(args, suggestions);
        }
        else if (args.length == 2 && args[0].equals("tracking"))
        {
            return getListOfStringsMatchingLastWord(args, "start", "stop", "restart", "realtime");
        }
        else if (args.length >= 2 && entityTypes.contains(args[0]))
        {
            List<String> detailSuggestions = Lists.newArrayList(SpecificDetailMode.getSuggestion());
            if (args.length == 2)
            {
                detailSuggestions.add("realtime");
                return getListOfStringsMatchingLastWord(args, detailSuggestions);
            }
            else if (detailSuggestions.contains(args[1]))
            {
                return getListOfStringsMatchingLastWord(args, "realtime");
            }
        }
        return Collections.emptyList();
    }
}
