package carpet.commands;

import carpet.helpers.CarpetUpdater;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandUpdateCarpet extends CommandBase {
    @Override
    public String getName()
    {
        return "updateCarpet";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/updateCarpet";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CarpetUpdater.updateCarpet(server);
    }
}
