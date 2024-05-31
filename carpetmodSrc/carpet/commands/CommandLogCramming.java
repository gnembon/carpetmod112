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

public class CommandLogCramming extends CommandCarpetBase {
    public static int numCrammed = 0;
    public static long initTime = 0;
    public static long endTime = 0;
    public static boolean counting = false;
    public String getName() {
        return "logcramming";
    }
    
    public String getUsage(ICommandSender sender) {
        return "/logcramming <start|stop|reset|show>";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || (!args[0].equals("start") && !args[0].equals("stop") && !args[0].equals("reset") && !args[0].equals("show"))) {
            throw new WrongUsageException("/export: expected one of 'start', 'stop', 'reset' or 'show'", new Object[0]);
        }
        if (args[0].equals("start")) {
            numCrammed = 0;
            counting = true;
            initTime = CarpetServer.minecraft_server.getTickCounter();
        } else if (args[0].equals("reset")) {
            numCrammed = 0;
            initTime = 0;
            endTime = 1;
        } else if (args[0].equals("stop")) {
            counting = false;
            endTime = CarpetServer.minecraft_server.getTickCounter();
        } else if (args[0].equals("show")) {
            if (counting) {
                endTime = CarpetServer.minecraft_server.getTickCounter();
            }
            double timeElapsed = endTime - initTime;
            notifyCommandListener(sender, this, Double.toString(numCrammed / (timeElapsed / 20.0 / 60.0 / 60.0)));
        } else {
            // ????!!! this is not possible
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "reset", "show");
        }
        return Collections.<String>emptyList();
    }
}

