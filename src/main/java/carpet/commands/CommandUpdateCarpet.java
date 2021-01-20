package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.CarpetUpdater;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
import net.minecraft.server.MinecraftServer;

public class CommandUpdateCarpet extends CommandCarpetBase {
    @Override
    public String method_29277()
    {
        return "updateCarpet";
    }

    @Override
    public String method_29275(class_2010 sender)
    {
        return "/updateCarpet";
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if(!CarpetSettings.updateCarpetAll && !sender.method_29603(2, "gamemode")){
            method_28710(sender, this, "Only a server-operator can perform this action");
            return;
        }
        CarpetUpdater.updateCarpet(server);
    }
}
