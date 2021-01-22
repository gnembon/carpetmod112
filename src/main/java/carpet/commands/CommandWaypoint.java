package carpet.commands;

import carpet.utils.Messenger;
import carpet.utils.Waypoint;
import carpet.utils.extensions.WaypointContainer;
import net.minecraft.*;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.InvalidNumberException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandWaypoint extends CommandCarpetBase {
    public static final String USAGE = "/waypoint <add|remove|list> ...";
    public static final String USAGE_ADD = "/waypoint add <name> [x y z] [dimension] [yaw pitch]";
    public static final String USAGE_LIST = "/waypoint list [<dimension>|<user>|all] [page]";
    public static final String USAGE_REMOVE = "/waypoint remove <waypoint>";

    @Override
    public String method_29277() {
        return "waypoint";
    }

    @Override
    public String method_29275(CommandSource sender) {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (!command_enabled("commandWaypoint", sender))
            return;

        if (args.length < 1) {
            throw new class_6182(USAGE);
        }

        switch (args[0]) {
            case "add":
                addWaypoint(sender, args);
                break;
            case "remove":
                removeWaypoint(sender, args);
                break;
            case "list":
                listWaypoints(sender, args);
                break;
            default:
                throw new class_6182(USAGE);
        }
    }

    public ServerWorld getDimension(CommandSource sender, String[] args, int offset) {
        if (args.length <= offset) return (ServerWorld) sender.getEntityWorld();
        String id = args[offset];
        MinecraftServer server = sender.getServer();
        switch (id) {
            case "overworld": return server.getWorldById(0);
            case "the_nether": case "nether": return server.getWorldById(-1);
            case "the_end": case "end": return server.getWorldById(1);
        }
        return null;
    }

    private void addWaypoint(CommandSource sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new class_6182(USAGE_ADD);
        }
        String name = args[1];
        Vec3d pos = sender.getPosVector();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        ServerWorld dimension = getDimension(sender, args, 5);
        boolean validDimension = dimension != null;
        if (!validDimension) {
            dimension = (ServerWorld) sender.getEntityWorld();
        }
        if (((WaypointContainer) dimension).getWaypoints().containsKey(name)) {
            throw new CommandException("Waypoint already exists");
        }
        double yaw = 0;
        double pitch = 0;
        Entity senderEntity = sender.getCommandInvoker();
        if (senderEntity != null) {
            yaw = senderEntity.yaw;
            pitch = senderEntity.pitch;
        }
        if (args.length > 2) {
            if (args.length < 5) throw new class_6182(USAGE_ADD);
            x = method_28702(x, args[2], true).method_28750();
            y = method_28701(y, args[3], 0, dimension.method_26053(), false).method_28750();
            z = method_28702(z, args[4], true).method_28750();
            if (args.length > 5) {
                if (!validDimension) {
                    throw new CommandException("Invalid dimension");
                }
                if (args.length > 6) {
                    if (args.length < 8) throw new class_6182(USAGE_ADD);
                    yaw = method_28702(yaw, args[6], false).method_28750();
                    pitch = method_28702(pitch, args[7], false).method_28750();
                }
            }
        }
        Waypoint w = new Waypoint(dimension, name, sender.getName(), x, y, z, yaw, pitch);
        ((WaypointContainer) dimension).getWaypoints().put(name, w);
        Messenger.m(sender, "w Waypoint ", "w " + w.getDimension().getSaveDir(), "g :", "y " + w.name + " ", Messenger.tp("c", w), "w  added");
    }

    private void removeWaypoint(CommandSource sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new class_6182(USAGE_REMOVE);
        }
        Waypoint w = Waypoint.find(args[1], (ServerWorld) sender.getEntityWorld(), sender.getServer().worlds);
        if (w == null) {
            throw new CommandException("Waypoint not found");
        }
        if (!w.canManipulate(sender)) {
            throw new CommandException("You are not allowed to remove this waypoint");
        }
        ((WaypointContainer) w.world).getWaypoints().remove(w.name);
        Messenger.s(sender, "Waypoint removed");
    }

    private void listWaypoints(CommandSource sender, String[] args) throws CommandException {
        if (args.length > 3) {
            throw new class_6182(USAGE_LIST);
        }
        List<Waypoint> waypoints = new ArrayList<>();
        ServerWorld dimension = getDimension(sender, args, 1);
        boolean validDimension = dimension != null;
        if (!validDimension) {
            dimension = (ServerWorld) sender.getEntityWorld();
        }
        Text header = new LiteralText("Waypoints in the " + dimension.dimension.getType().getSaveDir().replace("the_", ""));
        boolean printDimension = true;
        boolean printCreator = true;
        if (args.length > 1) {
            if (validDimension) {
                printDimension = false;
                waypoints.addAll(((WaypointContainer) dimension).getWaypoints().values());
            } else if ("all".equalsIgnoreCase(args[1])) {
                header = new LiteralText("All waypoints");
                for (ServerWorld w : sender.getServer().worlds) {
                    waypoints.addAll(((WaypointContainer) w).getWaypoints().values());
                }
            } else {
                printCreator = false;
                header = Messenger.m(null, "w Waypoints by ", "e " + args[1]);
                for (ServerWorld w : sender.getServer().worlds) {
                    for (Waypoint wp : ((WaypointContainer) w).getWaypoints().values()) {
                        if (args[1].equalsIgnoreCase(wp.creator)) waypoints.add(wp);
                    }
                }

            }
        } else {
            printDimension = false;
            waypoints.addAll(((WaypointContainer) dimension).getWaypoints().values());
        }
        int PAGE_SIZE = 20;
        int total = waypoints.size();
        if (total == 0) {
            Messenger.s(sender, "No waypoints found");
            return;
        }
        int pages = MathHelper.ceil(total / (float) PAGE_SIZE);
        int page = 0;
        if (args.length > 2) {
            try {
                page = Integer.parseInt(args[2]) - 1;
            } catch (NumberFormatException e) {
                throw new InvalidNumberException("Invalid page number");
            }
            if (page < 0 || page >= pages) {
                throw new CommandException("Page number out of range");
            }
        }
        if (pages > 1) {
            header.append(" (page " + (page + 1) + "/" + pages + ") ");
            if (page > 0) {
                Text prevPage = new LiteralText("[<]");
                Style s = prevPage.getStyle();
                s.setColor(Formatting.DARK_GRAY);
                s.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Previous page")));
                s.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint list " + args[1] + " " + page));
                header.append(" ").append(prevPage);
            }
            if (page + 1 < pages) {
                Text nextPage = new LiteralText("[>]");
                Style s = nextPage.getStyle();
                s.setColor(Formatting.GRAY);
                s.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Next page")));
                s.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint list " + args[1] + " " + (page + 2)));
                header.append(" ").append(nextPage);
            }
        }
        header.append(":");
        sender.sendSystemMessage(header);
        waypoints = waypoints.subList(page * PAGE_SIZE, Math.min((page + 1) * PAGE_SIZE, waypoints.size()));
        for (Waypoint w : waypoints) {
            if (printDimension) {
                if (printCreator && w.creator != null) {
                    Messenger.m(sender, "w " + w.getDimension().getSaveDir(), "g :", "y " + w.name + " ", Messenger.tp("c", w), "w  by ", "e " + w.creator);
                } else {
                    Messenger.m(sender, "w " + w.getDimension().getSaveDir(), "g :", "y " + w.name + " ", Messenger.tp("c", w));
                }
            } else {
                if (printCreator && w.creator != null) {
                    Messenger.m(sender, "y " + w.name + " ", Messenger.tp("c", w), "w  by ", "e " + w.creator);
                } else {
                    Messenger.m(sender, "y " + w.name + " ", Messenger.tp("c", w));
                }
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 0) return Arrays.asList("add", "list", "remove");
        if (args.length == 1) return method_28732(args, "add", "list", "remove");
        switch (args[0]) {
            case "list": {
                if (args.length == 2) {
                    Set<String> users = new TreeSet<>();
                    for (ServerWorld world : server.worlds) {
                        for (Waypoint w : ((WaypointContainer) world).getWaypoints().values()) {
                            if (w.creator != null) users.add(w.creator);
                        }
                    }
                    List<String> possibleArgs = new ArrayList<>(users);
                    possibleArgs.addAll(0, Arrays.asList("all", "overworld", "nether", "end"));
                    return method_28731(args, possibleArgs);
                }
                break;
            }
            case "add": {
                switch (args.length) {
                    case 3: case 4: case 5: return method_28730(args, 2, targetPos);
                    case 6: return method_28732(args, "overworld", "nether", "end");
                }
                break;
            }
            case "remove": {
                if (args.length == 2) {
                    Set<String> waypointNames = Waypoint.getAllWaypoints(sender.getServer().worlds).stream()
                            .filter(w -> w.canManipulate(sender))
                            .flatMap(w -> Stream.of(w.name, w.getFullName()))
                            .collect(Collectors.toCollection(TreeSet::new));
                    return method_28731(args, waypointNames);
                }
                break;
            }
        }
        return Collections.emptyList();
    }
}