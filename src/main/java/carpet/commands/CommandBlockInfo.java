package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.BlockInfo;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandBlockInfo extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */

    @Override
    public String method_29275(CommandSource sender)
    {
        return "Usage: blockinfo <X> <Y> <Z>";
    }

    @Override
    public String method_29277()
    {
        return "blockinfo";
    }

    /**
     * Callback for when the command is executed
     */
    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandBlockInfo", sender)) return;

        if (args.length != 3)
        {
            throw new class_6182(method_29275(sender));
        }
        BlockPos blockpos = method_28713(sender, args, 0, false);
        World world = sender.getEntityWorld();
        msg(sender, BlockInfo.blockInfo(blockpos, world));
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandBlockInfo)
        {
            return Collections.<String>emptyList();
        }
        if (args.length > 0 && args.length <= 3)
        {
            return method_28730(args, 0, pos);
        }
        return Collections.<String>emptyList();
    }
}
