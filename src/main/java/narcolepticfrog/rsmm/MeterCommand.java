package narcolepticfrog.rsmm;

import carpet.commands.CommandCarpetBase;
import narcolepticfrog.rsmm.server.RSMMServer;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import java.util.Collections;
import java.util.List;

public class MeterCommand extends CommandCarpetBase
{
    RSMMServer rsmmServer;

    public MeterCommand(RSMMServer rsmmServer) {
        this.rsmmServer = rsmmServer;
    }

    @Override
    public String method_29277() {
        return "meter";
    }

    private static final String USAGE = "/meter name [idx] name OR /meter color [idx] <RRGGBB> OR /meter removeAll Or" +
            " /meter group <groupName> OR /meter listGroups";

    @Override
    public String method_29275(class_2010 sender) {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if (!command_enabled("redstoneMultimeter", sender)) return;
        if (args.length < 1) {
            throw new class_6182(USAGE);
        }

        if (!(sender instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity)sender;

        if (args[0].equals("name")) {
            if (rsmmServer.getNumMeters(player) <= 0) {
                throw new class_6175("There are no meters to rename!");
            }
            if (args.length == 2) {
                rsmmServer.renameLastMeter(player, args[1]);
                notifySender(sender, "Renamed meter to " + args[1]);
            } else if (args.length == 3) {
                int ix = method_28719(args[1], 0, rsmmServer.getNumMeters(player) - 1);
                rsmmServer.renameMeter(player, ix, args[2]);
                notifySender(sender, "Renamed meter " + ix + " to " + args[2]);
            } else {
                throw new class_6182(USAGE);
            }
        } else if (args[0].equals("color")) {
            if (rsmmServer.getNumMeters(player) <= 0) {
                throw new class_6175("There are no meters to recolor!");
            }
            if (args.length == 2) {
                rsmmServer.recolorLastMeter(player, ColorUtils.parseColor(args[1]));
                notifySender(sender, "Recolored meter to " + args[1]);
            } else if (args.length == 3) {
                int ix = method_28719(args[1], 0, rsmmServer.getNumMeters(player) - 1);
                rsmmServer.recolorMeter(player, ix, ColorUtils.parseColor(args[2]));
                notifySender(sender, "Recolored meter " + ix + " to " + args[2]);
            } else {
                throw new class_6182(USAGE);
            }
        } else if (args[0].equals("removeAll")) {

            if (args.length != 1) {
                throw new class_6182(USAGE);
            }
            rsmmServer.removeAllMeters(player);
            notifySender(sender, "Removed all meters.");

        } else if (args[0].equals("group")) {

            if (args.length != 2) {
                throw new class_6182(USAGE);
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
            throw new class_6182(USAGE);
        }
    }

    public void notifySender(class_2010 sender, String message) {
        LiteralText messageText = new LiteralText(message);
        messageText.getStyle().setColor(Formatting.GRAY);
        sender.sendMessage(messageText);
    }

    @Override
    public List<String> method_29273(MinecraftServer server,
                                          class_2010 sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return method_28732(args, "name", "color", "removeAll", "group", "listGroups");
        } else if (args.length == 2 && args[0].equals("group")) {
            return method_28731(args, rsmmServer.getGroupNames());
        } else {
            return Collections.<String>emptyList();
        }
    }
}
