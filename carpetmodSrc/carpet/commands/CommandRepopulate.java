package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandRepopulate extends CommandCarpetBase {
    public static final String USAGE = "/repopulate <chunk x> <chunk z>";

    @Override
    public String getName()
    {
        return "repopulate";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return USAGE;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandRepopulate", sender))
            return;

        if (args.length != 2) {
            throw new WrongUsageException(USAGE);
        }
        int chunkX = parseInt(args[0]);
        int chunkZ = parseInt(args[1]);
        boolean isloaded = sender.getEntityWorld().isChunkLoaded(chunkX, chunkZ, false);
        Chunk chunk = sender.getEntityWorld().getChunk(chunkX, chunkZ);
        chunk.setUnpopulated();
        if (isloaded){
            sender.sendMessage(new TextComponentString("Marked currently loaded chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        } else {
            sender.sendMessage(new TextComponentString("Marked chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        }
    }

    @Override
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
