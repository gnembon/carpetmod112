package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;

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

}
