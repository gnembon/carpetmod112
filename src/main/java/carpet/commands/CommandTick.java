package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.utils.CarpetProfiler;
import net.minecraft.class_6182;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientMessageHandler;
import carpet.helpers.TickSpeed;


public class CommandTick extends CommandCarpetBase
{
    @Override
    public String method_29277()
    {
        return "tick";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return "Usage: tick rate <tickrate in tps> | warp [time in ticks to skip]";
    }

    @Override
    public void method_29272(final MinecraftServer server, final CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandTick", sender)) return;
        if (args.length == 0)
        {
            throw new class_6182(method_29275(sender));
        }
        if ("rate".equalsIgnoreCase(args[0]))
        {
            if (args.length == 2)
            {
                float tickrate = (float) method_28716(args[1], 0.01D);
                TickSpeed.tickrate(tickrate);
            }
            CarpetClientMessageHandler.sendTickRateChanges();
            method_28710(sender, this, String.format("tick rate is %.1f", TickSpeed.tickrate));
            return;
        }
        else if ("warp".equalsIgnoreCase(args[0]))
        {
            long advance = args.length >= 2 ? method_28720(args[1], 0, Long.MAX_VALUE) : TickSpeed.time_bias > 0 ? 0 : Long.MAX_VALUE;
            PlayerEntity player = null;
            if (sender instanceof PlayerEntity)
            {
                player = (PlayerEntity)sender;
            }

            String s = null;
            CommandSource icommandsender = null;
            if (args.length > 3)
            {
                s = method_28729(args, 2);
                icommandsender = sender;
            }

            String message = TickSpeed.tickrate_advance(player, advance, s, icommandsender);
            if (!message.isEmpty())
            {
                method_28710(sender, this, message);
            }
            return;
        }
        else if ("freeze".equalsIgnoreCase(args[0]))
        {
            TickSpeed.is_paused = !TickSpeed.is_paused;
            if (TickSpeed.is_paused)
            {
                method_28710(sender, this, "Game is paused");
            }
            else
            {
                method_28710(sender, this, "Game runs normally");
            }
            return;
        }
        else if ("step".equalsIgnoreCase(args[0]))
        {
            int advance = 1;
            if (args.length > 1)
            {
                advance = method_28719(args[1], 1, 72000);
            }
            TickSpeed.add_ticks_to_run_in_pause(advance);
            return;
        }
        else if ("superHot".equalsIgnoreCase(args[0]))
        {
            if (args.length > 1)
            {
                if ("stop".equalsIgnoreCase(args[1]) && !TickSpeed.is_superHot)
                {
                    return;
                }
                if ("start".equalsIgnoreCase(args[1]) && TickSpeed.is_superHot)
                {
                    return;
                }
            }
            TickSpeed.is_superHot = !TickSpeed.is_superHot;
            if (TickSpeed.is_superHot)
            {
                method_28710(sender, this, "Superhot enabled");
            }
            else
            {
                method_28710(sender, this, "Superhot disabled");
            }
            return;
        }
        else if ("health".equalsIgnoreCase(args[0]))
        {
            int step = 100;
            if (args.length > 1)
            {
                step = method_28719(args[1], 20, 72000);
            }
            CarpetProfiler.prepare_tick_report(step);
            return;
        }
        else if ("entities".equalsIgnoreCase(args[0]))
        {
            int step = 100;
            if (args.length > 1)
            {
                step = method_28719(args[1], 20, 72000);
            }
            CarpetProfiler.prepare_entity_report(step);
            return;
        }
        throw new class_6182(method_29275(sender));
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandTick)
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            return method_28732(args, "rate","warp", "freeze", "step", "superHot", "health", "entities");
        }
        if (args.length == 2 && "superHot".equalsIgnoreCase(args[0]))
        {
            return method_28732(args, "stop","start");
        }
        if (args.length == 2 && "rate".equalsIgnoreCase(args[0]))
        {
            return method_28732(args, "20");
        }
        if (args.length == 2 && "warp".equalsIgnoreCase(args[0]))
        {
            return method_28732(args, "1000","24000","72000");
        }
        if (args.length == 2 && "health".equalsIgnoreCase(args[0]))
        {
            return method_28732(args, "100","200","1000");
        }
        if (args.length == 2 && "entities".equalsIgnoreCase(args[0]))
        {
            return method_28732(args, "100","200","1000");
        }
        return Collections.emptyList();
    }
}
