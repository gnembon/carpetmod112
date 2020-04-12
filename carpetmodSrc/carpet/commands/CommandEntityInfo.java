package carpet.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.EntityInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.RayTraceResult;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class CommandEntityInfo extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "entityinfo";
    }

    /**
     * Gets the usage string for the command.
     *  
     * @param sender The ICommandSender who is requesting usage details
     */
    public String getUsage(ICommandSender sender)
    {
        return "Usage: entityinfo <entity_selector>";
    }


    public void print_multi_message(List<String> messages, ICommandSender sender, String grep)
    {
        List<String> actual = new ArrayList<String>();
        if (grep != null)
        {
            Pattern p = Pattern.compile(grep);
            actual.add(messages.get(0));
            boolean empty = true;
            for (int i = 1; i<messages.size(); i++)
            {
                String line = messages.get(i);
                Matcher m = p.matcher(line);
                if (m.find())
                {
                    empty = false;
                    actual.add(line);
                }
            }
            if (empty)
            {
                return;
            }
        }
        else
        {
            actual = messages;
        }
        notifyCommandListener(sender, this, "");
        for (String lline: actual)
        {
            notifyCommandListener(sender, this, lline);
        }
    }

    /**
     * Callback for when the command is executed
     *
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args The arguments that were passed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandEntityInfo", sender)) return;
        if (args.length == 0 || "grep".equalsIgnoreCase(args[0]))
        {
            String grep = null;
            if (args.length == 2)
            {
                grep = args[1];
            }
            EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
            List<String> report = EntityInfo.entityInfo(entityplayer, sender.getEntityWorld());
            print_multi_message(report, sender, grep);
        }
        else
        {
            Entity entity = getEntity(server, sender, args[0]);
            //LOG.error("SENDER dimension "+ sender.getEntityWorld().provider.getDimensionType().getId());
            List<String> report = EntityInfo.entityInfo(entity, sender.getEntityWorld());
            String grep = null;
            if (args.length >= 3 && "grep".equalsIgnoreCase(args[1]))
            {
                grep = args[2];
            }
            print_multi_message(report, sender, grep);
        }
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     *  
     * @param args The arguments of the command invocation
     * @param index The index
     */
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (!CarpetSettings.commandEntityInfo)
        {
            notifyCommandListener(sender, this, "Command is disabled in carpet settings");
        }
        List<String> list = getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        RayTraceResult result = ((EntityPlayerMP)sender).actionPack.mouseOver();
        if(result != null && result.typeOfHit == RayTraceResult.Type.ENTITY){
            list.add(result.entityHit.getUniqueID().toString());
        }
        return args.length == 1 ? list : Collections.emptyList();
    }
}
