package carpet.commands;

import carpet.helpers.LazyChunkBehaviorHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;


public class CommandLazyChunkBehavior extends CommandCarpetBase{


    /**
     * Gets the name of the command
     */

    public String getUsage(ICommandSender sender)
    {
        return "Usage: lazychunkbehavior <X> <Z>";
    }

    public String getName()
    {
        return "lazychunkbehavior";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("CommandLazyChunkBehavior", sender)) return;

        if (args.length != 2)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        int chunkX = parseInt(args[0]);
        int chunkZ = parseInt(args[1]);
        World world = sender.getEntityWorld();
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        sender.sendMessage(new TextComponentString(  "\u00A74\u00A7lChunk " + chunkX + ", " + chunkZ + " " + LazyChunkBehaviorHelper.CommandFeedBack(chunk)));
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
