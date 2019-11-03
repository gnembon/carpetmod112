package carpet.commands;

import carpet.utils.Messenger;
import carpet.utils.Waypoint;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.WorldServer;

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
    public String getName() {
        return "waypoint";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandWaypoint", sender))
            return;

        if (args.length < 1) {
            throw new WrongUsageException(USAGE);
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
                throw new WrongUsageException(USAGE);
        }
    }

    public WorldServer getDimension(ICommandSender sender, String[] args, int offset) {
        if (args.length <= offset) return (WorldServer) sender.getEntityWorld();
        String id = args[offset];
        MinecraftServer server = sender.getServer();
        switch (id) {
            case "overworld": return server.getWorld(0);
            case "the_nether": case "nether": return server.getWorld(-1);
            case "the_end": case "end": return server.getWorld(1);
        }
        return null;
    }

    private void addWaypoint(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(USAGE_ADD);
        }
        String name = args[1];
        Vec3d pos = sender.getPositionVector();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        WorldServer dimension = getDimension(sender, args, 5);
        boolean validDimension = dimension != null;
        if (!validDimension) {
            dimension = (WorldServer) sender.getEntityWorld();
        }
        if (dimension.waypoints.containsKey(name)) {
            throw new CommandException("Waypoint already exists");
        }
        double yaw = 0;
        double pitch = 0;
        Entity senderEntity = sender.getCommandSenderEntity();
        if (senderEntity != null) {
            yaw = senderEntity.rotationYaw;
            pitch = senderEntity.rotationPitch;
        }
        if (args.length > 2) {
            if (args.length < 5) throw new WrongUsageException(USAGE_ADD);
            x = parseCoordinate(x, args[2], true).getResult();
            y = parseCoordinate(y, args[3], 0, dimension.getHeight(), false).getResult();
            z = parseCoordinate(z, args[4], true).getResult();
            if (args.length > 5) {
                if (!validDimension) {
                    throw new CommandException("Invalid dimension");
                }
                if (args.length > 6) {
                    if (args.length < 8) throw new WrongUsageException(USAGE_ADD);
                    yaw = parseCoordinate(yaw, args[6], false).getResult();
                    pitch = parseCoordinate(pitch, args[7], false).getResult();
                }
            }
        }
        Waypoint w = new Waypoint(dimension, name, sender.getName(), x, y, z, yaw, pitch);
        dimension.waypoints.put(name, w);
        Messenger.m(sender, "w Waypoint ", "w " + w.getDimension().getName(), "g :", "y " + w.name + " ", Messenger.tp("c", w), "w  added");
    }

    private void removeWaypoint(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(USAGE_REMOVE);
        }
        Waypoint w = Waypoint.find(args[1], (WorldServer) sender.getEntityWorld(), sender.getServer().worlds);
        if (w == null) {
            throw new CommandException("Waypoint not found");
        }
        if (!w.canManipulate(sender)) {
            throw new CommandException("You are not allowed to remove this waypoint");
        }
        w.world.waypoints.remove(w.name);
        Messenger.s(sender, "Waypoint removed");
    }

    private void listWaypoints(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 3) {
            throw new WrongUsageException(USAGE_LIST);
        }
        List<Waypoint> waypoints = new ArrayList<>();
        WorldServer dimension = getDimension(sender, args, 1);
        boolean validDimension = dimension != null;
        if (!validDimension) {
            dimension = (WorldServer) sender.getEntityWorld();
        }
        ITextComponent header = new TextComponentString("Waypoints in the " + dimension.provider.getDimensionType().getName().replace("the_", ""));
        boolean printDimension = true;
        boolean printCreator = true;
        if (args.length > 1) {
            if (validDimension) {
                printDimension = false;
                waypoints.addAll(dimension.waypoints.values());
            } else if ("all".equalsIgnoreCase(args[1])) {
                header = new TextComponentString("All waypoints");
                for (WorldServer w : sender.getServer().worlds) {
                    waypoints.addAll(w.waypoints.values());
                }
            } else {
                printCreator = false;
                header = Messenger.m(null, "w Waypoints by ", "e " + args[1]);
                for (WorldServer w : sender.getServer().worlds) {
                    for (Waypoint wp : w.waypoints.values()) {
                        if (args[1].equalsIgnoreCase(wp.creator)) waypoints.add(wp);
                    }
                }

            }
        } else {
            printDimension = false;
            waypoints.addAll(dimension.waypoints.values());
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
                throw new SyntaxErrorException("Invalid page number");
            }
            if (page < 0 || page >= pages) {
                throw new CommandException("Page number out of range");
            }
        }
        if (pages > 1) {
            header.appendText(" (page " + (page + 1) + "/" + pages + ") ");
            if (page > 0) {
                ITextComponent prevPage = new TextComponentString("[<]");
                Style s = prevPage.getStyle();
                s.setColor(TextFormatting.DARK_GRAY);
                s.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Previous page")));
                s.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint list " + args[1] + " " + page));
                header.appendText(" ").appendSibling(prevPage);
            }
            if (page + 1 < pages) {
                ITextComponent nextPage = new TextComponentString("[>]");
                Style s = nextPage.getStyle();
                s.setColor(TextFormatting.GRAY);
                s.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Next page")));
                s.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint list " + args[1] + " " + (page + 2)));
                header.appendText(" ").appendSibling(nextPage);
            }
        }
        header.appendText(":");
        sender.sendMessage(header);
        waypoints = waypoints.subList(page * PAGE_SIZE, Math.min((page + 1) * PAGE_SIZE, waypoints.size()));
        for (Waypoint w : waypoints) {
            if (printDimension) {
                if (printCreator && w.creator != null) {
                    Messenger.m(sender, "w " + w.getDimension().getName(), "g :", "y " + w.name + " ", Messenger.tp("c", w), "w  by ", "e " + w.creator);
                } else {
                    Messenger.m(sender, "w " + w.getDimension().getName(), "g :", "y " + w.name + " ", Messenger.tp("c", w));
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

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 0) return Arrays.asList("add", "list", "remove");
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "add", "list", "remove");
        switch (args[0]) {
            case "list": {
                if (args.length == 2) {
                    Set<String> users = new TreeSet<>();
                    for (WorldServer world : server.worlds) {
                        for (Waypoint w : world.waypoints.values()) {
                            if (w.creator != null) users.add(w.creator);
                        }
                    }
                    List<String> possibleArgs = new ArrayList<>(users);
                    possibleArgs.addAll(0, Arrays.asList("all", "overworld", "nether", "end"));
                    return getListOfStringsMatchingLastWord(args, possibleArgs);
                }
                break;
            }
            case "add": {
                switch (args.length) {
                    case 3: case 4: case 5: return getTabCompletionCoordinate(args, 2, targetPos);
                    case 6: return getListOfStringsMatchingLastWord(args, "overworld", "nether", "end");
                }
                break;
            }
            case "remove": {
                if (args.length == 2) {
                    Set<String> waypointNames = Waypoint.getAllWaypoints(sender.getServer().worlds).stream()
                            .filter(w -> w.canManipulate(sender))
                            .flatMap(w -> Stream.of(w.name, w.getFullName()))
                            .collect(Collectors.toCollection(TreeSet::new));
                    return getListOfStringsMatchingLastWord(args, waypointNames);
                }
                break;
            }
        }
        return Collections.emptyList();
    }
}