package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.CarpetUpdater;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandUpdateCarpet extends CommandCarpetBase {
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
        if(!CarpetSettings.updateCarpetAll && !sender.canUseCommand(2, "gamemode")){
            notifyCommandListener(sender, this, "Only a server-operator can perform this action");
            return;
        }
        CarpetUpdater.updateCarpet(server);
    }
}
