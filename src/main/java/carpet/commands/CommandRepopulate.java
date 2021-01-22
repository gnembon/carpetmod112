package carpet.commands;

import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.extensions.RepopulatableChunk;
import net.minecraft.class_6182;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandRepopulate extends CommandCarpetBase {
    public static final String USAGE = "/repopulate <chunk x> <chunk z>";

    @Override
    public String method_29277()
    {
        return "repopulate";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandRepopulate", sender))
            return;

        if (args.length != 2) {
            throw new class_6182(USAGE);
        }
        int chunkX = method_28715(args[0]);
        int chunkZ = method_28715(args[1]);
        boolean isloaded = ((WorldAccessor) sender.getEntityWorld()).invokeIsChunkLoaded(chunkX, chunkZ, false);
        Chunk chunk = sender.getEntityWorld().method_25975(chunkX, chunkZ);
        ((RepopulatableChunk) chunk).setUnpopulated();
        if (isloaded){
            sender.sendSystemMessage(new LiteralText("Marked currently loaded chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        } else {
            sender.sendSystemMessage(new LiteralText("Marked chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        int chunkX = sender.getBlockPos().getX() >> 4;
        int chunkZ = sender.getBlockPos().getZ() >> 4;

        if (args.length == 1) {
            return method_28732(args, Integer.toString(chunkX));
        } else if (args.length == 2) {
            return method_28732(args, Integer.toString(chunkZ));
        } else {
            return Collections.emptyList();
        }
    }

}
