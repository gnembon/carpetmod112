package carpet.commands;

import net.minecraft.class_6175;
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
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
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
            throw new class_6175("Only a player can have a ping!");
        }
    }

}
