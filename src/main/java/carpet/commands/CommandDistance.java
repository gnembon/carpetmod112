package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.DistanceCalculator;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandDistance extends CommandCarpetBase
{
    @Override
    public String method_29275(class_2010 sender)
    {
        return "Usage: distance <X1> <Y1> <Z1> <X2> <Y2> <Z2>";
    }

    @Override
    public String method_29277()
    {
        return "distance";
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandDistance", sender)) return;
        if (args.length != 6)
        {
            throw new class_6182(method_29275(sender), new Object[0]);
        }
        BlockPos blockpos = method_28713(sender, args, 0, false);
        BlockPos blockpos2 = method_28713(sender, args, 3, false);
        msg(sender, DistanceCalculator.print_distance_two_points(blockpos, blockpos2));

    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandDistance)
        {
            return Collections.<String>emptyList();
        }
        if (args.length > 0 && args.length <= 3)
        {
            return method_28730(args, 0, pos);
        }
        if (args.length > 3 && args.length <= 6)
        {
            return method_28730(args, 3, pos);
        }
        return Collections.<String>emptyList();
    }
}
