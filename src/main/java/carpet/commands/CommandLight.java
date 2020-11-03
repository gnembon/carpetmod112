package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class CommandLight extends CommandCarpetBase {
    public static BlockPos tntScanPos = null;
    public static final String USAGE = "/light [x1 y1 z1] [x2 y2 z2] <sky/block> <value>";

    @Override
    public String getName() {
        return "light";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!command_enabled("commandLight", sender)) return;

        int x1, y1, z1, x2, y2, z2, lightLevel;
        String type;
        if (args.length > 7) {
            if (args.length > 8) throw new WrongUsageException(USAGE);
            x1 = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[0], false).getResult());
            y1 = (int) Math.round(parseCoordinate(sender.getPosition().getY(), args[1], false).getResult());
            z1 = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[2], false).getResult());

            x2 = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[3], false).getResult());
            y2 = (int) Math.round(parseCoordinate(sender.getPosition().getY(), args[4], false).getResult());
            z2 = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[5], false).getResult());

            type = args[6];
            try {
                lightLevel = Integer.parseInt(args[7]);
            } catch (Exception e) {
                throw new WrongUsageException(USAGE);
            }

            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }
            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }
            if (z1 > z2) {
                int temp = z1;
                z1 = z2;
                z2 = temp;
            }

            EnumSkyBlock t;

            if (type.equals("sky")) {
                t = EnumSkyBlock.SKY;
            } else if (type.equals("block")) {
                t = EnumSkyBlock.BLOCK;
            } else {
                throw new WrongUsageException(USAGE);
            }
            fillLightInArea(sender.getEntityWorld(), t, x1, y1, z1, x2, y2, z2, lightLevel);

            notifyCommandListener(sender, this,
                    String.format("Changing light level of %s to %d from:[%d %d %d] to:[%d %d %d]", type, lightLevel, x1, y1, z1, x2, y2, z2));
        } else {
            throw new WrongUsageException(USAGE);
        }
    }

    private void fillLightInArea(World world, EnumSkyBlock type, int x1, int y1, int z1, int x2, int y2, int z2, int lightLevel) {
        for (int z = z1; z <= z2; z++) {
            for (int y = y1; y <= y2; y++) {
                for (int x = x1; x <= x2; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    world.setLightFor(type, pos, lightLevel);
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 0) {
            return Collections.emptyList();
        } else if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getX()));
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getY()));
        } else if (args.length == 3) {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getZ()));
        } else if (args.length == 4) {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getX()));
        } else if (args.length == 5) {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getY()));
        } else if (args.length == 6) {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getZ()));
        } else if (args.length == 7) {
            return getListOfStringsMatchingLastWord(args, "sky", "block");
        } else if (args.length == 8) {
            return getListOfStringsMatchingLastWord(args, "0", "15");
        } else {
            return Collections.emptyList();
        }
    }
}
