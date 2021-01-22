package carpet.commands;

import carpet.helpers.LagSpikeHelper;
import com.google.common.collect.Collections2;
import net.minecraft.class_6182;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandLagSpike extends CommandCarpetBase {
    private static final long MAX_LAG_TIME = 60000;

    @Override
    public String method_29277() {
        return "lagspike";
    }

    @Override
    public String method_29275(CommandSource sender) {
        return "/lagspike <seconds> [tick_phase] [sub_phase] [dimension]";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (!command_enabled("commandLagspike", sender)) {
            return;
        }

        if (args.length < 1) {
            throw new class_6182(method_29275(sender));
        }

        // don't allow a lag spike which will trigger the watchdog
        long maxMillis = (long) ((double)((MinecraftDedicatedServer) server).method_33396() * 0.9);
        if (maxMillis <= 0) {
            maxMillis = MAX_LAG_TIME;
        } else if (maxMillis > MAX_LAG_TIME) {
            maxMillis = MAX_LAG_TIME;
        }
        int seconds = method_28719(args[0], 1, MathHelper.ceil((double)maxMillis / 1000));
        long millis = (long)seconds * 1000;
        if (millis > maxMillis) {
            millis = maxMillis;
        }

        LagSpikeHelper.TickPhase phase = args.length > 2 ? parseEnum(args[1], LagSpikeHelper.TickPhase.class) : LagSpikeHelper.TickPhase.TICK;
        Enum<?> subPhase = args.length > 3 ? parseEnumUnchecked(args[2], phase.getSubPhaseClass()) : args.length > 2 ? phase.getDefaultSubPhase() : LagSpikeHelper.PrePostSubPhase.POST;

        DimensionType dimension;
        if (phase.isDimensionApplicable()) {
            try {
                dimension = args.length > 4 ? DimensionType.method_27530(args[3]) : DimensionType.OVERWORLD;
            } catch (IllegalArgumentException e) {
                throw new CommandException("Invalid dimension: " + args[3]);
            }
        } else {
            dimension = null;
        }

        LagSpikeHelper.addLagSpike(dimension, phase, subPhase, millis);
        method_28710(sender, this, "Lagging the server for " + seconds + " seconds in phase " + phase.name().toLowerCase(Locale.ROOT) + "/" + subPhase.name().toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T parseEnumUnchecked(String arg, Class<? extends Enum<?>> clazz) throws CommandException {
        return parseEnum(arg, (Class<T>) clazz);
    }

    private static <T extends Enum<T>> T parseEnum(String arg, Class<T> clazz) throws CommandException {
        arg = arg.toUpperCase(Locale.ROOT);
        for (T val : clazz.getEnumConstants()) {
            if (val.name().equals(arg)) {
                return val;
            }
        }
        throw new CommandException("Invalid value: " + arg);
    }

    private static Collection<String> getEnumCompletions(Class<? extends Enum<?>> clazz) {
        return Collections2.transform(Arrays.asList(clazz.getEnumConstants()), val -> val == null ? "null" : val.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2) {
            return method_28731(args, getEnumCompletions(LagSpikeHelper.TickPhase.class));
        } else if (args.length >= 3) {
            LagSpikeHelper.TickPhase phase;
            try {
                phase = parseEnum(args[1], LagSpikeHelper.TickPhase.class);
            } catch (CommandException e) {
                return Collections.emptyList();
            }

            if (args.length == 3) {
                return method_28731(args, getEnumCompletions(phase.getSubPhaseClass()));
            }

            if (args.length == 4 && phase.isDimensionApplicable()) {
                return method_28731(args, Collections2.transform(Arrays.asList(DimensionType.values()), dimensionType -> dimensionType != null ? dimensionType.getSaveDir() : null));
            }
        }

        return Collections.emptyList();
    }
}
