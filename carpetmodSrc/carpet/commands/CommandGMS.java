package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

import net.minecraft.command.WrongUsageException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;

import net.minecraft.potion.Potion;

public class CommandGMS extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "s";
    }

    /**
     * Gets the usage string for the command.
     *  
     * @param sender The ICommandSender who is requesting usage details
     */
    public String getUsage(ICommandSender sender)
    {
        return "commands.gamemode.usage";
    }

    /**
     * Callback for when the command is executed
     *  
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args The arguments that were passed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, NumberInvalidException
    {
        if (!command_enabled("commandCameramode", sender)) return;
        if (args.length > 0)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        else
        {
            EntityPlayerMP entityplayer = getCommandSenderAsPlayer(sender);
            setPlayerToSurvival(server, entityplayer);
        }
    }

    public static void setPlayerToSurvival(MinecraftServer server, EntityPlayerMP entityplayer) {
        GameType gametype = server.getGameType();
        if(entityplayer.interactionManager.getGameType() != GameType.SURVIVAL) {
            if (entityplayer instanceof EntityPlayerMP) entityplayer.moveToStoredCameraData();
            entityplayer.setGameType(gametype);
            entityplayer.removePotionEffect(Potion.getPotionFromResourceLocation("night_vision"));
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.<String>emptyList();
    }


}
