package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.IronFarmOptimization;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class CommandVillage extends CommandCarpetBase {
    public static final String USAGE = "/village <dimension> order <best|worst|random>\n" +
            " OR /village <dimension> undo\n" +
            " OR /village <dimension> move <start> <count> <destination>\n" +
            " OR /village <dimension> centers\n" +
            " OR /village <dimension> load\n" +
            " OR /village <dimension> remove <*|start> [end]";
    public static final String USAGE_ORDER = "/village <overworld|nether|end|*> order <best|worst|random>";
    public static final String USAGE_UNDO = "/village <overworld|nether|end|*> undo";
    public static final String USAGE_MOVE = "/village <overworld|nether|end|*> move <start> <count> <destination>";
    public static final String USAGE_CENTERS = "/village <overworld|nether|end|*> centers";
    public static final String USAGE_LOAD = "/village <overworld|nether|end|*> load";
    public static final String USAGE_REMOVE = "/village <overworld|nether|end|*> remove * OR /village <overworld|nether|end|*> remove <index> OR /village <overworld|nether|end|*> remove <start> <end>";

    @Override
    public String getName() {
        return "village";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandVillage", sender)) return;
        if (args.length < 2) throw new WrongUsageException(USAGE);

        List<WorldServer> worlds = parseDimension(server, args);
        String dim;
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "overworld":
            case "0":
                dim = "Overworld";
                break;
            case "nether":
            case "-1":
                dim = "Nether";
                break;
            case "end":
            case "1":
                dim = "End";
                break;
            case "*":
            case "all":
                dim = "All dimensions";
                break;
            default:
                dim = "Error";
        }

        switch (args[1].toLowerCase(Locale.ENGLISH)) {
            case "order": // Reorders village list
                if (args.length != 3) throw new WrongUsageException(USAGE_ORDER);

                for (WorldServer world : worlds) {
                    VillageCollection villageCollection = world.getVillageCollection();
                    List<Village> villages = villageCollection.getVillageList();
                    villageCollection.oldVillageList = new ArrayList<>(villages);

                    switch (args[2].toLowerCase(Locale.ENGLISH)) {
                        case "best":
                            villages.sort(Comparator.comparingInt(Village::getVillageRadius));
                            break;
                        case "worst":
                            villages.sort(Comparator.comparingInt(Village::getVillageRadius).reversed());
                            break;
                        case "random":
                            Collections.shuffle(villages);
                            break;
                        default:
                            throw new CommandException(String.format("'%s' is not a valid option", args[1]));
                    }
                }
                notifyCommandListener(sender, this, String.format("%s village list modified", dim));
                break;
            case "undo": // Undos last modification to the village list
                if (args.length != 2) throw new WrongUsageException(USAGE_UNDO);

                for (WorldServer world : worlds) {
                    VillageCollection villageCollection = world.getVillageCollection();
                    villageCollection.getVillageList().sort(Comparator.comparingInt(village -> {
                        int index = villageCollection.oldVillageList.indexOf(village);
                        return index == -1 ? Integer.MAX_VALUE : index;
                    }));
                }
                notifyCommandListener(sender, this, String.format("%s village list modified", dim));
                break;
            case "move": // Moves 'count' villages starting at 'start' to 'destination'
                if (args.length != 5) throw new WrongUsageException(USAGE_MOVE);

                int start = parseInt(args[2], 0);
                int count = parseInt(args[3], 1);
                int destination = parseInt(args[4], 0);
                int from;
                int to;
                int distance;

                if (destination < start) {
                    from = destination;
                    to = start + count;
                    distance = count;
                } else {
                    from = start;
                    to = destination;
                    distance = -count;
                }
                for (WorldServer world : worlds) {
                    VillageCollection villageCollection = world.getVillageCollection();
                    List<Village> villages = villageCollection.getVillageList();
                    villageCollection.oldVillageList = new ArrayList<>(villages);
                    if (villages.size() < start + count || villages.size() < destination)
                        throw new WrongUsageException("Index larger than number of villages");
                    Collections.rotate(villages.subList(from, to), distance);
                }
                notifyCommandListener(sender, this, String.format("%s village list modified", dim));
                break;
            case "centers": // Lists all village centers
                if (args.length != 2) throw new WrongUsageException(USAGE_CENTERS);

                int total = 0;
                Map<BlockPos, Integer> centers = new HashMap<>();
                for (WorldServer world : worlds) {
                    List<Village> villages = world.getVillageCollection().getVillageList();
                    for (Village village : villages) {
                        centers.merge(village.getCenter(), 1, Integer::sum);
                    }
                    total += villages.size();
                }
                if (centers.size() > 16) {
                    Map<BlockPos, Map<Integer, Integer>> flatMap = new HashMap<>();
                    centers.forEach((center, value) ->
                            flatMap.compute(new BlockPos(center.getX(), 0, center.getZ()), (key, old) -> {
                                Map<Integer, Integer> map = old;
                                if (old == null) map = new HashMap<>();
                                map.merge(-1, value, Integer::sum);
                                map.merge(center.getY(), value, Integer::sum);
                                return map;
                            })
                    );
                    if (flatMap.size() > 16) {
                        List<Set<BlockPos>> groups = new ArrayList<>();
                        for (BlockPos center : flatMap.keySet()) {
                            BlockPos north = center.north();
                            BlockPos east = center.east();
                            BlockPos south = center.south();
                            BlockPos west = center.west();
                            Set<BlockPos> belongs = null;
                            for (BlockPos offset : new BlockPos[]{north, east, south, west}) {
                                Iterator<Set<BlockPos>> itr = groups.iterator();
                                while (itr.hasNext()) {
                                    Set<BlockPos> group = itr.next();
                                    if (group.contains(offset)) {
                                        if (belongs == null) {
                                            belongs = group;
                                        } else {
                                            belongs.addAll(group);
                                            itr.remove();
                                        }
                                        break;
                                    }
                                }
                            }
                            if (belongs == null) {
                                Set<BlockPos> set = new HashSet<>();
                                set.add(center);
                                groups.add(set);
                            }
                        }
                        for (Set<BlockPos> group : groups) {
                            int sum = 0;
                            int minX = Integer.MAX_VALUE;
                            int minY = Integer.MAX_VALUE;
                            int minZ = Integer.MAX_VALUE;
                            int maxX = Integer.MIN_VALUE;
                            int maxY = Integer.MIN_VALUE;
                            int maxZ = Integer.MIN_VALUE;
                            for (BlockPos pos : group) {
                                Map<Integer, Integer> column = flatMap.get(pos);
                                for (int y : column.keySet()) {
                                    if (y == -1) {
                                        sum += column.get(y);
                                    } else {
                                        if (y < minY) minY = y;
                                        if (y > maxY) maxY = y;
                                    }
                                }
                                int x = pos.getX();
                                if (x < minX) minX = x;
                                if (x > maxX) maxX = x;
                                int z = pos.getZ();
                                if (z < minZ) minZ = z;
                                if (z > maxZ) maxZ = z;
                            }
                            notifyCommandListener(sender, this, String.format("x: %d-%d, y: %d-%d, z: %d-%d, Centers: %d",
                                    minX, maxX, minY, maxY, minZ, maxZ, sum));
                        }
                    } else {
                        flatMap.forEach((center, value) -> {
                            int min = Integer.MAX_VALUE;
                            int max = Integer.MIN_VALUE;
                            for (int num : value.keySet()) {
                                if (num == -1) continue;
                                if (num < min) min = num;
                                if (num > max) max = num;
                            }
                            notifyCommandListener(sender, this, String.format("x: %d, y: %d-%d, z: %d, Centers: %d",
                                    center.getX(), min, max, center.getZ(), value.get(-1)));
                        });
                    }
                } else {
                    centers.forEach((center, value) ->
                            notifyCommandListener(sender, this, String.format("x: %d, y: %d, z: %d, Centers: %d",
                                    center.getX(), center.getY(), center.getZ(), value)));
                }
                notifyCommandListener(sender, this, String.format("Villages: %d", total));
                break;
            case "load": // Adds villages from villages.dat
                if (args.length != 2) throw new WrongUsageException(USAGE_LOAD);

                File file = new File("villages.dat");
                if (file.exists()) {
                    try (FileInputStream stream = new FileInputStream(file)) {
                        NBTTagCompound comp = CompressedStreamTools.readCompressed(stream);
                        NBTTagCompound data = comp.getCompoundTag("data");
                        NBTTagList tagList = data.getTagList("Villages", 10);

                        for (WorldServer world : worlds) {
                            VillageCollection villageCollection = world.getVillageCollection();
                            int tickCounter = villageCollection.getTickCounter();
                            List<Village> villages = villageCollection.getVillageList();

                            for (int i = 0; i < tagList.tagCount(); ++i) {
                                NBTTagCompound compound = tagList.getCompoundTagAt(i);
                                Village village = new Village(world);
                                villages.add(village);

                                village.readVillageDataFromNBT(compound);
                                village.getVillageDoorInfoList().forEach(door -> door.setLastActivityTimestamp(tickCounter));

                                for (VillageDoorInfo door : village.getVillageDoorInfoList()) {
                                    if (CarpetSettings.doorSearchOptimization)
                                        IronFarmOptimization.addDoor(villageCollection, village, door.getDoorBlockPos());
                                    if (CarpetSettings.doorCheckOptimization)
                                        IronFarmOptimization.addChunk(villageCollection.getVillageChunks(), door.getDoorBlockPos());
                                }
                            }
                            villageCollection.markDirty();
                        }
                        VillageCollection.updateMarkers = true;
                    } catch (IOException e) {
                        throw new CommandException("Invalid villages.dat");
                    }
                } else {
                    throw new CommandException("villages.dat not found");
                }
                notifyCommandListener(sender, this, String.format("Villages added to %s", dim.toLowerCase(Locale.ENGLISH)));
                break;
            case "remove": // Removes villages
                if (args.length != 3 && args.length != 4) throw new WrongUsageException(USAGE_REMOVE);

                try {
                    switch (args.length) {
                        case 3:
                            if (args[2].equalsIgnoreCase("*")) {
                                worlds.forEach(world ->
                                        world.getVillageCollection().getVillageList().forEach(village -> village.getVillageDoorInfoList().clear()));
                                notifyCommandListener(sender, this, String.format("All villages removed from %s", dim.toLowerCase(Locale.ENGLISH)));
                            } else {
                                int index = parseInt(args[2]);
                                worlds.forEach(world ->
                                        world.getVillageCollection().getVillageList().get(index).getVillageDoorInfoList().clear());
                                notifyCommandListener(sender, this, String.format("Village removed from %s", dim.toLowerCase(Locale.ENGLISH)));
                            }
                            break;
                        case 4:
                            int i = parseInt(args[2]);
                            int end = parseInt(args[3], i);
                            int k = 0;
                            for (WorldServer world : worlds) {
                                VillageCollection villageCollection = world.getVillageCollection();
                                List<Village> villages = villageCollection.getVillageList();
                                for (; i < end; i++) {
                                    villages.get(i).getVillageDoorInfoList().clear();
                                    k++;
                                }
                            }
                            notifyCommandListener(sender, this, String.format("Removed %d villages from %s", k, dim.toLowerCase(Locale.ENGLISH)));
                            break;
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw new NumberInvalidException("commands.generic.num.invalid", args[2]);
                }
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private List<WorldServer> parseDimension(MinecraftServer server, String[] args) throws CommandException {
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "overworld":
            case "0":
                return Collections.singletonList(server.getWorld(0));
            case "nether":
            case "-1":
                return Collections.singletonList(server.getWorld(-1));
            case "end":
            case "1":
                return Collections.singletonList(server.getWorld(1));
            case "*":
            case "all":
                return Arrays.asList(server.worlds);
            default:
                throw new CommandException(String.format("'%s' is not a valid dimension", args[0]));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (!CarpetSettings.commandVillage) return Collections.emptyList();

        switch (args.length) {
            case 1:
                return getListOfStringsMatchingLastWord(args, "overworld", "nether", "end", "*");
            case 2:
                return getListOfStringsMatchingLastWord(args, "order", "undo", "move", "centers", "load", "remove");
            case 3:
                if ("order".equalsIgnoreCase(args[1]))
                    return getListOfStringsMatchingLastWord(args, "best", "worse", "random");
                break;
        }
        return Collections.emptyList();
    }
}
