package carpet.commands;

import carpet.CarpetSettings;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommandScoreboardPublic extends ScoreboardCommand {
    @Override
    public String method_29277() {
        return "scoreboardPublic";
    }

    @Override
    public String method_29275(CommandSource sender) {
        return "/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]";
    }

    @Override
    public int method_28700() {
        return 0;
    }

    @Override
    public boolean method_29271(MinecraftServer server, CommandSource sender) {
        return true;
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175 {
        if (!CarpetSettings.commandPublicScoreboard) return;

        if (args.length < 1) {
            throw new class_6182("commands.scoreboard.usage");
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 1) {
                    throw new class_6182("/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]");
                }

                if ("list".equalsIgnoreCase(args[1])) {
                    this.method_31844(sender, server);
                } else if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length < 4) {
                        throw new class_6182("/scoreboardPublic objectives add [objective]");
                    }

                    this.method_31845(sender, args, 2, server);
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length != 3) {
                        throw new class_6182("/scoreboardPublic objectives remove [objective]");
                    }

                    this.method_31843(sender, args[2], server);
                } else {
                    if (!"setdisplay".equalsIgnoreCase(args[1])) {
                        throw new class_6182("/scoreboardPublic objectives setdisplay [objective]");
                    }

                    if (args.length != 3 && args.length != 4) {
                        throw new class_6182("/scoreboardPublic objectives setdisplay <slot> [objective]");
                    }

                    this.method_31860(sender, args, 2, server);
                }
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        if (!CarpetSettings.commandPublicScoreboard) return Collections.emptyList();

        if (args.length == 1) {
            return method_28732(args, "objectives");
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 2) {
                    return method_28732(args, "list", "add", "remove", "setdisplay");
                }

                if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length == 4) {
                        Set<String> set = ScoreboardCriterions.field_26748.keySet();
                        return method_28731(args, set);
                    }
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return method_28731(args, this.method_31850(false, server));
                    }
                } else if ("setdisplay".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return method_28732(args, Scoreboard.getDisplaySlotNames());
                    }

                    if (args.length == 4) {
                        return method_28731(args, this.method_31850(false, server));
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
