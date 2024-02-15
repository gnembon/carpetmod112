package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.GolemCounter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandGolem extends CommandCarpetBase {
    /**
     * Gets the name of the command
     */
    public String getUsage(ICommandSender sender) {
        return "/golem [full|reset|realtime]";
    }

    public String getName() {
        return "golem";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("golemCounter", sender)) return;

        GolemCounter counter = GolemCounter.counter;
        if (args.length == 0) {
            msg(sender, counter.format(server, false, false, true));
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "full":
                msg(sender, counter.format(server, false, false, false));
                return;
            case "realtime":
                msg(sender, counter.format(server, true, false, true));
                return;
            case "reset":
                counter.reset(server);
                notifyCommandListener(sender, this, "Golem counter restarted.");
                return;
        }
        throw new WrongUsageException(getUsage(sender));
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (!CarpetSettings.golemCounter) return Collections.emptyList();

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "full", "reset", "realtime");
        }
        return Collections.emptyList();
    }
}
