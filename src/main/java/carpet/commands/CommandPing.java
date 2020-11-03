package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandPing extends CommandCarpetBase
{

    @Override
    public String getName()
    {
        return "ping";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/ping";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandPing", sender))
            return;

        if (sender instanceof EntityPlayerMP)
        {
            int ping = ((EntityPlayerMP) sender).ping;
            sender.sendMessage(new TextComponentString("Your ping is: " + ping + " ms"));
        }
        else
        {
            throw new CommandException("Only a player can have a ping!");
        }
    }

}
