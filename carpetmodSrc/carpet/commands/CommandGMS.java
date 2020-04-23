package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
            if(LoggerRegistry.__invisDebug){ // Added debugger for the complex bug turning players invisible. CARPET-XCOM
                LoggerRegistry.getLogger("invisDebug").log(()-> new ITextComponent[]{
                        Messenger.s(null, "s1: " + entityplayer.world.loadedEntityList.contains(entityplayer))
                });
            }
            if (entityplayer instanceof EntityPlayerMP) {
                if(entityplayer.moveToStoredCameraData()){
                    if(LoggerRegistry.__invisDebug){ // Added debugger for the complex bug turning players invisible. CARPET-XCOM
                        LoggerRegistry.getLogger("invisDebug").log(()-> new ITextComponent[]{
                                Messenger.s(null, "s7: " + entityplayer.world.loadedEntityList.contains(entityplayer))
                        });
                    }
                    return;
                }
            }
            entityplayer.fallDistance = 0;
            if(LoggerRegistry.__invisDebug){ // Added debugger for the complex bug turning players invisible. CARPET-XCOM
                LoggerRegistry.getLogger("invisDebug").log(()-> new ITextComponent[]{
                        Messenger.s(null, "s5: " + entityplayer.world.loadedEntityList.contains(entityplayer))
                });
            }
            entityplayer.setGameType(gametype);
            if(!entityplayer.hadNightvision()) entityplayer.removePotionEffect(Potion.getPotionFromResourceLocation("night_vision"));
            if(LoggerRegistry.__invisDebug){ // Added debugger for the complex bug turning players invisible. CARPET-XCOM
                LoggerRegistry.getLogger("invisDebug").log(()-> new ITextComponent[]{
                        Messenger.s(null, "s6: " + entityplayer.world.loadedEntityList.contains(entityplayer))
                });
            }
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.<String>emptyList();
    }


}
