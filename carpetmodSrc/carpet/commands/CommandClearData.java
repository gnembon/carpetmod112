package carpet.commands;

import carpet.utils.Data;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Stack;

public class CommandClearData extends CommandCarpetBase {
    @Override
    public String getName() {
        return "clearData";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Data.times.clear();
        Data.glassArrivalTimes.clear();
    }
}