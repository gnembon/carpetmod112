package carpet.commands;

import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.extensions.RepopulatableChunk;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
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
    public String method_29275(class_2010 sender)
    {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandRepopulate", sender))
            return;

        if (args.length != 2) {
            throw new class_6182(USAGE);
        }
        int chunkX = method_28715(args[0]);
        int chunkZ = method_28715(args[1]);
        boolean isloaded = ((WorldAccessor) sender.method_29608()).invokeIsChunkLoaded(chunkX, chunkZ, false);
        WorldChunk chunk = sender.method_29608().method_25975(chunkX, chunkZ);
        ((RepopulatableChunk) chunk).setUnpopulated();
        if (isloaded){
            sender.sendMessage(new LiteralText("Marked currently loaded chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        } else {
            sender.sendMessage(new LiteralText("Marked chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args, @Nullable BlockPos targetPos) {
        int chunkX = sender.method_29606().getX() >> 4;
        int chunkZ = sender.method_29606().getZ() >> 4;

        if (args.length == 1) {
            return method_28732(args, Integer.toString(chunkX));
        } else if (args.length == 2) {
            return method_28732(args, Integer.toString(chunkZ));
        } else {
            return Collections.emptyList();
        }
    }

}
