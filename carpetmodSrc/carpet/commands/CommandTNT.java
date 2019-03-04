package carpet.commands;

import carpet.helpers.OptimizedExplosion;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandTNT extends CommandCarpetBase{
    public static BlockPos tntScanPos = null;
    public static final String USAGE = "/tnt [x y z]/clear";

    @Override
    public String getName() {
        return "tnt";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int x;
        int y;
        int z;
        if (args.length > 2) {
            if (args.length > 3) throw new WrongUsageException(USAGE);
            x = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[0], false).getResult());
            y = (int) Math.round(parseCoordinate(sender.getPosition().getY(), args[1], false).getResult());
            z = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[2], false).getResult());
            tntScanPos = new BlockPos(x, y, z);
            OptimizedExplosion.setBlastChanceLocation(tntScanPos);
            notifyCommandListener(sender, this,
                    String.format("TNT scanning block at: %d %d %d", x, y, z));
        } else if(args[0].equals("clear")){
            tntScanPos = null;
            notifyCommandListener(sender, this,
                    String.format("TNT scanning block cleared."));
        }else {
            throw new WrongUsageException(USAGE);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getX()), "clear");
        }
        else if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getY()));
        }
        else if (args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getZ()));
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
