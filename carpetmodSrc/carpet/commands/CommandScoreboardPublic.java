package carpet.commands;

import carpet.CarpetSettings;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandScoreboard;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommandScoreboardPublic extends CommandScoreboard {
    @Override
    public String getName() {
        return "scoreboardPublic";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!CarpetSettings.commandPublicScoreboard) return;

        if (args.length < 1) {
            throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 1) {
                    throw new WrongUsageException("/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]");
                }

                if ("list".equalsIgnoreCase(args[1])) {
                    this.listObjectives(sender, server);
                } else if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length < 4) {
                        throw new WrongUsageException("/scoreboardPublic objectives add [objective]");
                    }

                    this.addObjective(sender, args, 2, server);
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length != 3) {
                        throw new WrongUsageException("/scoreboardPublic objectives remove [objective]");
                    }

                    this.removeObjective(sender, args[2], server);
                } else {
                    if (!"setdisplay".equalsIgnoreCase(args[1])) {
                        throw new WrongUsageException("/scoreboardPublic objectives setdisplay [objective]");
                    }

                    if (args.length != 3 && args.length != 4) {
                        throw new WrongUsageException("/scoreboardPublic objectives setdisplay <slot> [objective]");
                    }

                    this.setDisplayObjective(sender, args, 2, server);
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (!CarpetSettings.commandPublicScoreboard) return Collections.<String>emptyList();

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[]{"objectives"});
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 2) {
                    return getListOfStringsMatchingLastWord(args, new String[]{"list", "add", "remove", "setdisplay"});
                }

                if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length == 4) {
                        Set<String> set = IScoreCriteria.INSTANCES.keySet();
                        return getListOfStringsMatchingLastWord(args, set);
                    }
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(false, server));
                    }
                } else if ("setdisplay".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, Scoreboard.getDisplaySlotStrings());
                    }

                    if (args.length == 4) {
                        return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(false, server));
                    }
                }
            }
        }
        return Collections.<String>emptyList();
    }
}
