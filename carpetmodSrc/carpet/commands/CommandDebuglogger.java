package carpet.commands;

import carpet.CarpetSettings;
import carpet.logging.LogHandler;
import carpet.logging.Logger;
import carpet.logging.LoggerOptions;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class CommandDebuglogger extends CommandLog {

    private final String USAGE = "/debuglogger (interactive menu) OR /log <logName> [?option] [player] [handler ...] OR /log <logName> clear [player] OR /log defaults (interactive menu) OR /log setDefault <logName> [?option] [handler ...] OR /log removeDefault <logName>";

    @Override
    public String getName() {
        return "logdebug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }
}
