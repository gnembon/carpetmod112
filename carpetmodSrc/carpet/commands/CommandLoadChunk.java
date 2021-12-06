package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.BlockInfo;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandLoadChunk extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */

    public String getUsage(ICommandSender sender)
    {
        return "Usage: loadchunk <X> <Z>";
    }

    public String getName()
    {
        return "loadchunk";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandLoadChunk", sender)) return;

        if (args.length != 2)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        int chunkX = parseInt(args[0]);
        int chunkZ = parseInt(args[1]);
        World world = sender.getEntityWorld();
        world.getChunk(chunkX, chunkZ);
        sender.sendMessage(new TextComponentString("Chunk" + chunkX + ", " + chunkZ + " loaded"));
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        int chunkX = sender.getPosition().getX() >> 4;
        int chunkZ = sender.getPosition().getZ() >> 4;

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Integer.toString(chunkX));
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, Integer.toString(chunkZ));
        } else {
            return Collections.emptyList();
        }
    }
}
