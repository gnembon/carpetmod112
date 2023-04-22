package carpet.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import carpet.logging.LoggerRegistry;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;

import net.minecraft.command.WrongUsageException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;

import java.io.FileWriter;
import java.io.IOException;

public class CommandExport extends CommandCarpetBase {
    public static boolean shouldAddData = false;
    public static ArrayList<Datapoint> savedPoints = new ArrayList<Datapoint>();
    private static String counter = "";

    public static class Datapoint {
        public double iph;
        public double mspt;
        public long items;
        public long ticks;

        public Datapoint(long t, long i, double ih, double mt) {
            ticks = t;
            items = i;
            iph = ih;
            mspt = mt;
        }
    }
    
    public String getName() {
        return "export";
    }
    
    public String getUsage(ICommandSender sender) {
        return "/export <start <counter> | stop [filename]>";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || (!args[0].equals("start") && !args[0].equals("stop"))) {
            throw new WrongUsageException("/export: expected one of 'start' or 'stop'", new Object[0]);
        }
        boolean starting = args[0].equals("start");
        if (starting && args.length < 2) {
            throw new WrongUsageException("/export start <counter>", new Object[0]);
        }
        
        if (starting == shouldAddData) {
            notifyCommandListener(sender, this, shouldAddData ? "/export: already capturing data" : "/export: not capturing any data");
            return;
        }
        shouldAddData = starting;
        if (shouldAddData) {
            savedPoints.clear();
            if (!HopperCounter.COUNTERS.containsKey(args[1])) {
                throw new WrongUsageException("/export: unknown counter '" + args[1] + "'", new Object[0]);
            }
            counter = args[1];
            notifyCommandListener(sender, this, "Starting capture of counter " + counter + "...");
        } else {
            String name;
            if (args.length > 1) {
                name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (!name.endsWith(".csv")) {
                    name += ".csv";
                }
            } else {
                name = LocalDate.now() + "_" + LocalTime.now() + ".csv";
            }
            notifyCommandListener(sender, this, "Saving as " + name);
            try {
                FileWriter f = new FileWriter(name);
                for (Datapoint dp : savedPoints) {
                    f.write(dp.ticks + "," + dp.iph + "," + dp.items + "," + dp.mspt + "\n");
                }
                f.close();
            } catch (IOException e) {
                notifyCommandListener(sender, this, "Some IO error occurred, idk");
            }
        }
    }

    public static void addDatapoint() {
        if (CommandExport.shouldAddData) {
            double mspt = MathHelper.average(CarpetServer.minecraft_server.tickTimeArray) * 1.0E-6D;
            long total = HopperCounter.COUNTERS.get(counter).getTotalItems();
            long ticks = CarpetServer.minecraft_server.getTickCounter() - HopperCounter.COUNTERS.get(counter).getStartTick();
            Datapoint dp = new Datapoint(ticks, total, (double)total * 20 * 3600 / ticks, mspt);
            CommandExport.savedPoints.add(dp);
        }
    }
    
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop");
        } else if (args.length == 2 && args[0].equals("start")) {
            return new ArrayList<>(HopperCounter.COUNTERS.keySet());
        }
        return Collections.<String>emptyList();
    }
}
