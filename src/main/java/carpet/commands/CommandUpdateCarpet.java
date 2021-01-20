package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.CarpetUpdater;
import net.minecraft.class_6175;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

public class CommandUpdateCarpet extends CommandCarpetBase {
    @Override
    public String method_29277()
    {
        return "updateCarpet";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return "/updateCarpet";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
    {
        if(!CarpetSettings.updateCarpetAll && !sender.allowCommandExecution(2, "gamemode")){
            method_28710(sender, this, "Only a server-operator can perform this action");
            return;
        }
        CarpetUpdater.updateCarpet(server);
    }
}
