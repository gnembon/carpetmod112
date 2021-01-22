package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class CommandPing extends CommandCarpetBase
{

    @Override
    public String method_29277()
    {
        return "ping";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return "/ping";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandPing", sender))
            return;

        if (sender instanceof ServerPlayerEntity)
        {
            int ping = ((ServerPlayerEntity) sender).pingMilliseconds;
            sender.sendSystemMessage(new LiteralText("Your ping is: " + ping + " ms"));
        }
        else
        {
            throw new CommandException("Only a player can have a ping!");
        }
    }

}
