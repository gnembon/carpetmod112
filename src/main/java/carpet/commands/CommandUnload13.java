package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.ChunkLoading;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CommandUnload13 extends CommandCarpetBase
{
    @Override
    public String method_29275(class_2010 sender)
    {
        return "Usage: unload <brief|verbose|order|protect> <X1> <Y1> <Z1> [<x2> <y2> <z2>]";
    }

    @Override
    public String method_29277()
    {
        return "unload13";
    }

    public void print_multi_message(List<String> messages, class_2010 sender)
    {
        for (String line: messages)
        {
            method_28710(sender, this, line);
        }
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandUnload", sender)) return;
        if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 4 && args.length != 7)
        {
            throw new class_6182(method_29275(sender));
        }
        BlockPos pos = sender.method_29606();
        BlockPos pos2 = null;
        if (args.length == 0)
        {
            ServerWorld world = (ServerWorld) (sender.method_29608() );
            method_28710(sender, this, "Chunk unloading report for "+world.dimension.getType());
            List<String> report = ChunkLoading.test_save_chunks_113(world, pos, false);
            print_multi_message(report, sender);
            return;
        }
        boolean verbose = "verbose".equalsIgnoreCase(args[0]);
        boolean order = "order".equalsIgnoreCase(args[0]);
        boolean protect = args[0].startsWith("protect");
        boolean custom_dim = false;
        int custom_dim_id = 0;
        if (args.length >= 4)
        {
            pos = method_28713(sender, args, 1, false);
        }
        if (args.length >= 7)
        {
            pos2 = method_28713(sender, args, 4, false);
        }
        if ("overworld".equalsIgnoreCase(args[0]))
        {
            custom_dim = true;
            custom_dim_id = 0;
        }
        if ("nether".equalsIgnoreCase(args[0]))
        {
            custom_dim = true;
            custom_dim_id = -1;
        }
        if ("end".equalsIgnoreCase(args[0]))
        {
            custom_dim = true;
            custom_dim_id = 1;
        }
        if (custom_dim && args.length > 1)
        {
            if ("verbose".equalsIgnoreCase(args[1]))
            {
                verbose = true;
            }
        }

        if (order)
        {
            List<String> orders = ChunkLoading.check_unload_order_13((ServerWorld)sender.method_29608(), pos, pos2);
            print_multi_message(orders, sender);
            return;
        }
        if (protect)
        {
            List<String> orders = ChunkLoading.protect_13((ServerWorld)sender.method_29608(), pos, pos2, args[0]);
            print_multi_message(orders, sender);
            return;
        }
        ServerWorld world = (ServerWorld) (custom_dim?server.getWorldById(custom_dim_id):sender.method_29608() );
        method_28710(sender, this, "Chunk unloading report for "+world.dimension.getType());
        List<String> report = ChunkLoading.test_save_chunks_113(world, pos, verbose);
        print_multi_message(report, sender);
    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandUnload)
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            return method_28732(args, "verbose", "brief", "order", "nether", "overworld", "end", "protect");
        }
        if (args.length == 2 && ( "nether".equalsIgnoreCase(args[0]) || "overworld".equalsIgnoreCase(args[0]) || "end".equalsIgnoreCase(args[0]) ))
        {
            return method_28732(args, "verbose");
        }
        if (args.length > 1 && args.length <= 4)
        {
            return method_28730(args, 1, pos);
        }
        if (args.length > 4 && args.length <= 7)
        {
            return method_28730(args, 4, pos);
        }
        return Collections.emptyList();
    }
}
