package carpet.commands;

import net.minecraft.class_2010;
import net.minecraft.class_6175;
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
    public String method_29275(class_2010 sender)
    {
        return "/ping";
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandPing", sender))
            return;

        if (sender instanceof ServerPlayerEntity)
        {
            int ping = ((ServerPlayerEntity) sender).pingMilliseconds;
            sender.sendMessage(new LiteralText("Your ping is: " + ping + " ms"));
        }
        else
        {
            throw new class_6175("Only a player can have a ping!");
        }
    }

}
