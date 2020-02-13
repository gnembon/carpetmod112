package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.OptimizedTNT;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityTNTPrimed;
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
         if(args[0].equals("setSeed")){
             try {
                 EntityTNTPrimed.randAngle.setSeed(Long.parseLong(args[1]) ^ 0x5DEECE66DL);
                 notifyCommandListener(sender, this, "RNG TNT angle seed set to " + args[1] + (CarpetSettings.TNTAdjustableRandomAngle ? "" : " Enable TNTAdjustableRandomAngle rule or seed wont work."));
             } catch (Exception e) {
             }
        } else if(args[0].equals("clear")){
             tntScanPos = null;
             notifyCommandListener(sender, this,
                     String.format("TNT scanning block cleared."));
         } else if (args.length > 2) {
            if (args.length > 3) throw new WrongUsageException(USAGE);
            x = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[0], false).getResult());
            y = (int) Math.round(parseCoordinate(sender.getPosition().getY(), args[1], false).getResult());
            z = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[2], false).getResult());
            tntScanPos = new BlockPos(x, y, z);
            OptimizedTNT.setBlastChanceLocation(tntScanPos);
            notifyCommandListener(sender, this,
                    String.format("TNT scanning block at: %d %d %d", x, y, z));
        } else {
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
