package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.OptimizedTNT;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandTNT extends CommandCarpetBase{
    public static Random rand = new Random();
    public static BlockPos tntScanPos = null;
    public static final String USAGE = "/tnt [x y z]/clear";

    @Override
    public String method_29277() {
        return "tnt";
    }

    @Override
    public String method_29275(class_2010 sender) {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175 {
        int x;
        int y;
        int z;
         if(args[0].equals("setSeed")){
             try {
                 rand.setSeed(Long.parseLong(args[1]) ^ 0x5DEECE66DL);
                 method_28710(sender, this, "RNG TNT angle seed set to " + args[1] + (CarpetSettings.TNTAdjustableRandomAngle ? "" : " Enable TNTAdjustableRandomAngle rule or seed wont work."));
             } catch (Exception e) {
             }
        } else if(args[0].equals("clear")){
             tntScanPos = null;
             method_28710(sender, this, "TNT scanning block cleared.");
         } else if (args.length > 2) {
            if (args.length > 3) throw new class_6182(USAGE);
            x = (int) Math.round(method_28702(sender.method_29606().getX(), args[0], false).method_28750());
            y = (int) Math.round(method_28702(sender.method_29606().getY(), args[1], false).method_28750());
            z = (int) Math.round(method_28702(sender.method_29606().getZ(), args[2], false).method_28750());
            tntScanPos = new BlockPos(x, y, z);
            OptimizedTNT.setBlastChanceLocation(tntScanPos);
            method_28710(sender, this,
                    String.format("TNT scanning block at: %d %d %d", x, y, z));
        } else {
            throw new class_6182(USAGE);
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return method_28732(args, String.valueOf(targetPos.getX()), "clear");
        }
        else if (args.length == 2)
        {
            return method_28732(args, String.valueOf(targetPos.getY()));
        }
        else if (args.length == 3)
        {
            return method_28732(args, String.valueOf(targetPos.getZ()));
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
