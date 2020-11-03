package carpet.commands;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandKill;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandRemoveEntity extends CommandKill {
    public String getName()
    {
        return "removeEntity";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
            entityplayer.onKillCommand();
            notifyCommandListener(sender, this, "commands.kill.successful", new Object[] {entityplayer.getDisplayName()});
        }
        else
        {
            Entity entity = getEntity(server, sender, args[0]);
            entity.setDead();

            if (!(entity instanceof EntityPlayerMP))
            {
                EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
                WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getEntityWorld(), entity);
            }

            notifyCommandListener(sender, this, "commands.kill.successful", new Object[] {entity.getDisplayName()});
        }
    }

}
