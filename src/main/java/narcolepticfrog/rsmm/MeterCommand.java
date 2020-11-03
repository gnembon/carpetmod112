package narcolepticfrog.rsmm;

import carpet.commands.CommandCarpetBase;
import narcolepticfrog.rsmm.server.RSMMServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class MeterCommand extends CommandCarpetBase
{
    RSMMServer rsmmServer;

    public MeterCommand(RSMMServer rsmmServer) {
        this.rsmmServer = rsmmServer;
    }

    @Override
    public String getName() {
        return "meter";
    }

    private static final String USAGE = "/meter name [idx] name OR /meter color [idx] <RRGGBB> OR /meter removeAll Or" +
            " /meter group <groupName> OR /meter listGroups";

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("redstoneMultimeter", sender)) return;
        if (args.length < 1) {
            throw new WrongUsageException(USAGE);
        }

        if (!(sender instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)sender;

        if (args[0].equals("name")) {
            if (rsmmServer.getNumMeters(player) <= 0) {
                throw new CommandException("There are no meters to rename!");
            }
            if (args.length == 2) {
                rsmmServer.renameLastMeter(player, args[1]);
                notifySender(sender, "Renamed meter to " + args[1]);
            } else if (args.length == 3) {
                int ix = parseInt(args[1], 0, rsmmServer.getNumMeters(player) - 1);
                rsmmServer.renameMeter(player, ix, args[2]);
                notifySender(sender, "Renamed meter " + ix + " to " + args[2]);
            } else {
                throw new WrongUsageException(USAGE);
            }
        } else if (args[0].equals("color")) {
            if (rsmmServer.getNumMeters(player) <= 0) {
                throw new CommandException("There are no meters to recolor!");
            }
            if (args.length == 2) {
                rsmmServer.recolorLastMeter(player, ColorUtils.parseColor(args[1]));
                notifySender(sender, "Recolored meter to " + args[1]);
            } else if (args.length == 3) {
                int ix = parseInt(args[1], 0, rsmmServer.getNumMeters(player) - 1);
                rsmmServer.recolorMeter(player, ix, ColorUtils.parseColor(args[2]));
                notifySender(sender, "Recolored meter " + ix + " to " + args[2]);
            } else {
                throw new WrongUsageException(USAGE);
            }
        } else if (args[0].equals("removeAll")) {

            if (args.length != 1) {
                throw new WrongUsageException(USAGE);
            }
            rsmmServer.removeAllMeters(player);
            notifySender(sender, "Removed all meters.");

        } else if (args[0].equals("group")) {

            if (args.length != 2) {
                throw new WrongUsageException(USAGE);
            }
            rsmmServer.changePlayerSubscription(player, args[1]);
            notifySender(sender, "Subscribed to meter group " + args[1]);

        } else if (args[0].equals("listGroups")) {

            StringBuilder response = new StringBuilder();
            response.append("Meter Groups:");
            for (String name : rsmmServer.getGroupNames()) {
                response.append("\n  " + name);
            }
            notifySender(sender, response.toString());

        } else {
            throw new WrongUsageException(USAGE);
        }
    }

    public void notifySender(ICommandSender sender, String message) {
        TextComponentString messageText = new TextComponentString(message);
        messageText.getStyle().setColor(TextFormatting.GRAY);
        sender.sendMessage(messageText);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server,
                                          ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "name", "color", "removeAll", "group", "listGroups");
        } else if (args.length == 2 && args[0].equals("group")) {
            return getListOfStringsMatchingLastWord(args, rsmmServer.getGroupNames());
        } else {
            return Collections.<String>emptyList();
        }
    }
}
