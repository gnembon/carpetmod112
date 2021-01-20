package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import carpet.utils.SpawnReporter;
import carpet.helpers.TickSpeed;
import java.util.ArrayList;

public class CommandSpawn extends CommandCarpetBase
{
    @Override
    public String method_29275(CommandSource sender)
    {
        return "Usage:\nspawn list <X> <Y> <Z>\nspawn entities/rates <... | passive | hostile | ambient | water>\nspawn mobcaps <set <num>, nether, overworld, end>\nspawn tracking <.../stop/start/hostile/passive/water/ambient>\nspawn mocking <true/false>";
    }

    @Override
    public String method_29277()
    {
        return "spawn";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandSpawn", sender)) return;
        if (args.length == 0)
        {
            throw new class_6182(method_29275(sender));
        }
        World world = sender.getEntityWorld();
        if ("list".equalsIgnoreCase(args[0]))
        {
            BlockPos blockpos = method_28713(sender, args, 1, false);
            if (!world.canSetBlock(blockpos))
            {
                throw new class_6175("commands.setblock.outOfWorld");
            }
            else
            {
                msg(sender,SpawnReporter.report(blockpos, world));
                return;
            }
        }
        
        else if ("tracking".equalsIgnoreCase(args[0]))
        {
            if (args.length == 1)
            {
                msg(sender,  SpawnReporter.tracking_report(world));
                return;
            }
            else if ("start".equalsIgnoreCase(args[1]))
            {
                if (SpawnReporter.track_spawns == 0L)
                {
                    BlockPos lsl = null;
                    BlockPos usl = null;
                    if (args.length == 8)
                    {
                        BlockPos a = method_28713(sender, args, 2, false);
                        BlockPos b = method_28713(sender, args, 5, false);
                        lsl = new BlockPos(
                                Math.min(a.getX(), b.getX()),
                                Math.min(a.getY(), b.getY()),
                                Math.min(a.getZ(), b.getZ()) );
                        usl = new BlockPos(
                                Math.max(a.getX(), b.getX()),
                                Math.max(a.getY(), b.getY()),
                                Math.max(a.getZ(), b.getZ()) );
                    } else if (args.length != 2)
                    {
                        method_28710(sender, this, "Wrong syntax: /spawn tracking start <X1 Y1 Z1 X2 Y2 Z2>");
                        return;
                    }
                    SpawnReporter.reset_spawn_stats(false);
                    SpawnReporter.track_spawns = (long) world.getServer().getTicks();
                    SpawnReporter.lower_spawning_limit = lsl;
                    SpawnReporter.upper_spawning_limit = usl;
                    method_28710(sender, this, "Spawning tracking started.");
                }
                else
                {
                    method_28710(sender, this, "You are already tracking spawning.");
                }
            }
            else if ("stop".equalsIgnoreCase(args[1]))
            {
                msg(sender, SpawnReporter.tracking_report(world));
                SpawnReporter.reset_spawn_stats(false);
                SpawnReporter.track_spawns = 0L;
                SpawnReporter.lower_spawning_limit = null;
                SpawnReporter.upper_spawning_limit = null;
                method_28710(sender, this, "Spawning tracking stopped.");
            }
            else
            {
                msg(sender,SpawnReporter.recent_spawns(world, args[1]));
            }
            return;
        }
        else if ("test".equalsIgnoreCase(args[0]))
        {
            String counter = null;
            long warp = 72000;
            if (args.length >= 2)
            {
                
                warp = method_28719(args[1], 20, 720000);
                if (args.length >= 3)
                {
                    counter = args[2];
                }
            }
            //stop tracking
            SpawnReporter.reset_spawn_stats(false);
            //start tracking
            SpawnReporter.track_spawns = (long) server.getTicks();
            //counter reset
            if (counter == null) {
                HopperCounter.resetAll(server);
            } else {
                HopperCounter hopperCounter = HopperCounter.getCounter(counter);
                if (hopperCounter != null) hopperCounter.reset(server);
            }
            
            // tick warp 0
            TickSpeed.tickrate_advance(null, 0, null, null);
            // tick warp given player
            PlayerEntity player = null;
            if (sender instanceof PlayerEntity)
            {
                player = (PlayerEntity)sender;
            }
            TickSpeed.tickrate_advance(player, warp, null, sender);
            method_28710(sender, this, String.format("Started spawn test for %d ticks", warp));
            return;
            
        }
        else if ("mocking".equalsIgnoreCase(args[0]))
        {
            boolean domock = method_28744(args[1]);
            if (domock)
            {
                SpawnReporter.initialize_mocking();
                method_28710(sender, this, "Mock spawns started, Spawn statistics reset");
            }
            else
            {
                SpawnReporter.stop_mocking();
                method_28710(sender, this, "Normal mob spawning, Spawn statistics reset");
            }
            return;
        }
        else if ("rates".equalsIgnoreCase(args[0]))
        {
            if (args.length >= 2 && "reset".equalsIgnoreCase(args[1]))
            {
                for (SpawnGroup s: SpawnReporter.spawn_tries.keySet())
                {
                    SpawnReporter.spawn_tries.put(s,1);
                }
            }
            else if (args.length >= 3)
            {
                String str = args[1];
                int num = method_28719(args[2], 0, 1000);
                SpawnReporter.spawn_tries.put(SpawnReporter.get_creature_type_from_code(str), num);
            }
            if (sender instanceof ServerPlayerEntity)
            {
                msg(sender, SpawnReporter.print_general_mobcaps(world));
            }
            return;
        }
        else if ("mobcaps".equalsIgnoreCase(args[0]))
        {
            if (args.length == 1)
            {
                msg(sender, SpawnReporter.print_general_mobcaps(world));
                return;
            }
            if (args.length > 1)
            {
                switch (args[1])
                {
                    case "set":
                        if (args.length > 2)
                        {
                            int desired_mobcap = method_28718(args[2], 0);
                            double desired_ratio = (double)desired_mobcap/SpawnGroup.MONSTER.getCapacity();
                            SpawnReporter.mobcap_exponent = 4.0*Math.log(desired_ratio)/Math.log(2.0);
                            method_28710(sender, this, String.format("Mobcaps for hostile mobs changed to %d, other groups will follow", desired_mobcap));
                            return;
                        }
                        msg(sender, SpawnReporter.print_general_mobcaps(world));
                        return;
                    case "overworld":
                        msg(sender, SpawnReporter.printMobcapsForDimension(world,0,"overworld"));
                        return;
                    case "nether":
                        msg(sender, SpawnReporter.printMobcapsForDimension(world,-1,"nether"));
                        return;
                    case "end":
                        msg(sender, SpawnReporter.printMobcapsForDimension(world,1,"the end"));
                        return;

                }
            }


        }
        else if ("entities".equalsIgnoreCase(args[0]))
        {
            if (args.length == 1)
            {
                msg(sender, SpawnReporter.print_general_mobcaps(world));
                return;
            }
            else
            {
                msg(sender, SpawnReporter.printEntitiesByType(args[1], world));
                return;
            }
        }
        throw new class_6182(method_29275(sender));

    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandSpawn)
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            return method_28732(args, "list","mocking","tracking","mobcaps","rates", "entities", "test");
        }
        if ("list".equalsIgnoreCase(args[0]) && args.length <= 4)
        {
            return method_28730(args, 1, pos);
        }
        if (args.length == 2)
        {
            if ("tracking".equalsIgnoreCase(args[0]))
            {
                return method_28732(args, "start", "stop", "hostile", "passive","ambient","water");
            }
            if ("mocking".equalsIgnoreCase(args[0]))
            {
                return method_28732(args, "true", "false");
            }
            if ("entities".equalsIgnoreCase(args[0]))
            {
                return method_28732(args, "hostile", "passive","ambient","water");
            }
            if ("rates".equalsIgnoreCase(args[0]))
            {
                return method_28732(args, "reset", "hostile", "passive","ambient","water");
            }
            if ("mobcaps".equalsIgnoreCase(args[0]))
            {
                return method_28732(args, "set","nether","overworld","end");
            }
            if ("test".equalsIgnoreCase(args[0]))
            {
                return method_28732(args, "24000", "72000");
            }
        }
        if ("test".equalsIgnoreCase(args[0]) && (args.length == 3))
        {
            List<String> lst = new ArrayList<>();
            for (DyeColor clr : DyeColor.values())
            {
                lst.add(clr.toString());
            }
            String[] stockArr = new String[lst.size()];
            stockArr = lst.toArray(stockArr);
            return method_28732(args, stockArr);
        }
        if ("mobcaps".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1]) && (args.length == 3))
        {
            return method_28732(args, "70");
        }
        if ("tracking".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1]) && args.length > 2 && args.length <= 5)
        {
            return method_28730(args, 2, pos);
        }
        if ("tracking".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1]) && args.length > 5 && args.length <= 8)
        {
            return method_28730(args, 5, pos);
        }
        return Collections.emptyList();
    }
}
