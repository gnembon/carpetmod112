package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import carpet.utils.SpawnReporter;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import carpet.helpers.TickSpeed;

import net.minecraft.item.EnumDyeColor;
import java.util.ArrayList;

public class CommandSpawn extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getUsage(ICommandSender sender)
    {
        return "Usage:\nspawn list <X> <Y> <Z>\nspawn entities/rates <... | passive | hostile | ambient | water>\nspawn mobcaps <set <num>, nether, overworld, end>\nspawn tracking <.../stop/start/hostile/passive/water/ambient>\nspawn mocking <true/false>";
    }
    public String getName()
    {
        return "spawn";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandSpawn", sender)) return;
        if (args.length == 0)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        World world = sender.getEntityWorld();
        if ("list".equalsIgnoreCase(args[0]))
        {
            BlockPos blockpos = parseBlockPos(sender, args, 1, false);
            if (!world.isBlockLoaded(blockpos))
            {
                throw new CommandException("commands.setblock.outOfWorld", new Object[0]);
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
                        BlockPos a = parseBlockPos(sender, args, 2, false);
                        BlockPos b = parseBlockPos(sender, args, 5, false);
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
                        notifyCommandListener(sender, this, "Wrong syntax: /spawn tracking start <X1 Y1 Z1 X2 Y2 Z2>");
                        return;
                    }
                    SpawnReporter.reset_spawn_stats(false);
                    SpawnReporter.track_spawns = (long) world.getMinecraftServer().getTickCounter();
                    SpawnReporter.lower_spawning_limit = lsl;
                    SpawnReporter.upper_spawning_limit = usl;
                    notifyCommandListener(sender, this, "Spawning tracking started.");
                }
                else
                {
                    notifyCommandListener(sender, this, "You are already tracking spawning.");
                }
            }
            else if ("stop".equalsIgnoreCase(args[1]))
            {
                msg(sender, SpawnReporter.tracking_report(world));
                SpawnReporter.reset_spawn_stats(false);
                SpawnReporter.track_spawns = 0L;
                SpawnReporter.lower_spawning_limit = null;
                SpawnReporter.upper_spawning_limit = null;
                notifyCommandListener(sender, this, "Spawning tracking stopped.");
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
                
                warp = parseInt(args[1], 20, 720000);
                if (args.length >= 3)
                {
                    counter = args[2];
                }
            }
            //stop tracking
            SpawnReporter.reset_spawn_stats(false);
            //start tracking
            SpawnReporter.track_spawns = (long) server.getTickCounter();
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
            EntityPlayer player = null;
            if (sender instanceof EntityPlayer)
            {
                player = (EntityPlayer)sender;
            }
            TickSpeed.tickrate_advance(player, warp, null, sender);
            notifyCommandListener(sender, this, String.format("Started spawn test for %d ticks", warp));
            return;
            
        }
        else if ("mocking".equalsIgnoreCase(args[0]))
        {
            boolean domock = parseBoolean(args[1]);
            if (domock)
            {
                SpawnReporter.initialize_mocking();
                notifyCommandListener(sender, this, "Mock spawns started, Spawn statistics reset");
            }
            else
            {
                SpawnReporter.stop_mocking();
                notifyCommandListener(sender, this, "Normal mob spawning, Spawn statistics reset");
            }
            return;
        }
        else if ("rates".equalsIgnoreCase(args[0]))
        {
            if (args.length >= 2 && "reset".equalsIgnoreCase(args[1]))
            {
                for (String s: SpawnReporter.spawn_tries.keySet())
                {
                    SpawnReporter.spawn_tries.put(s,1);
                }
            }
            else if (args.length >= 3)
            {
                String str = args[1];
                String code = SpawnReporter.get_creature_code_from_string(str);
                int num = parseInt(args[2], 0, 1000);
                SpawnReporter.spawn_tries.put(code, num);
            }
            if (sender instanceof EntityPlayerMP)
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
                            int desired_mobcap = parseInt(args[2], 0);
                            double desired_ratio = (double)desired_mobcap/EnumCreatureType.MONSTER.getMaxNumberOfCreature();
                            SpawnReporter.mobcap_exponent = 4.0*Math.log(desired_ratio)/Math.log(2.0);
                            notifyCommandListener(sender, this, String.format("Mobcaps for hostile mobs changed to %d, other groups will follow", desired_mobcap));
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
        throw new WrongUsageException(getUsage(sender), new Object[0]);

    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandSpawn)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "list","mocking","tracking","mobcaps","rates", "entities", "test");
        }
        if ("list".equalsIgnoreCase(args[0]) && args.length <= 4)
        {
            return getTabCompletionCoordinate(args, 1, pos);
        }
        if (args.length == 2)
        {
            if ("tracking".equalsIgnoreCase(args[0]))
            {
                return getListOfStringsMatchingLastWord(args, "start", "stop", "hostile", "passive","ambient","water");
            }
            if ("mocking".equalsIgnoreCase(args[0]))
            {
                return getListOfStringsMatchingLastWord(args, "true", "false");
            }
            if ("entities".equalsIgnoreCase(args[0]))
            {
                return getListOfStringsMatchingLastWord(args, "hostile", "passive","ambient","water");
            }
            if ("rates".equalsIgnoreCase(args[0]))
            {
                return getListOfStringsMatchingLastWord(args, "reset", "hostile", "passive","ambient","water");
            }
            if ("mobcaps".equalsIgnoreCase(args[0]))
            {
                return getListOfStringsMatchingLastWord(args, "set","nether","overworld","end");
            }
            if ("test".equalsIgnoreCase(args[0]))
            {
                return getListOfStringsMatchingLastWord(args, "24000", "72000");
                
            }
        }
        if ("test".equalsIgnoreCase(args[0]) && (args.length == 3))
        {
            List<String> lst = new ArrayList<String>();
            for (EnumDyeColor clr : EnumDyeColor.values())
            {
                lst.add(clr.toString());
            }
            String[] stockArr = new String[lst.size()];
            stockArr = lst.toArray(stockArr);
            return getListOfStringsMatchingLastWord(args, stockArr);
        }
        if ("mobcaps".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1]) && (args.length == 3))
        {
            return getListOfStringsMatchingLastWord(args, "70");
        }
        if ("tracking".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1]) && args.length > 2 && args.length <= 5)
        {
            return getTabCompletionCoordinate(args, 2, pos);
        }
        if ("tracking".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1]) && args.length > 5 && args.length <= 8)
        {
            return getTabCompletionCoordinate(args, 5, pos);
        }
        return Collections.<String>emptyList();
    }
}
