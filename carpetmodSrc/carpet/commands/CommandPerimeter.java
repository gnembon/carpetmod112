package carpet.commands;

import carpet.utils.Messenger;
import carpet.utils.PerimeterDiagnostics;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandPerimeter extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "perimetercheck";
    }

    /**
     * Gets the usage string for the command.
     */
    public String getUsage(ICommandSender sender)
    {
        return "/perimetercheck <X> <Y> <Z> <target_entity?>";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandPerimeterInfo", sender)) return;
        if (args.length < 1)
        {
            throw new WrongUsageException(getUsage(sender));
        }
        else
        {
            String s = args[0];
            BlockPos blockpos = sender.getPosition();
            Vec3d vec3d = sender.getPositionVector();
            double d0 = vec3d.x;
            double d1 = vec3d.y;
            double d2 = vec3d.z;
            if (args.length >= 3)
            {
                d0 = parseDouble(d0, args[0], true);
                d1 = parseDouble(d1, args[1], false);
                d2 = parseDouble(d2, args[2], true);
                blockpos = new BlockPos(d0, d1, d2);
            }
            World world = sender.getEntityWorld();
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            EntityLiving entityliving = null;
            if (args.length >= 4)
            {
                s = args[3];
                nbttagcompound.setString("id", s);
                entityliving = (EntityLiving) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, world, d0, d1+2, d2, true);
                if (entityliving == null)
                {
                    throw new CommandException("Failed to test entity");
                }
            }
            PerimeterDiagnostics.Result res = PerimeterDiagnostics.countSpots((WorldServer) world, blockpos, entityliving);
            if (sender instanceof EntityPlayer)
            {
                Messenger.m((EntityPlayer)sender, "w Spawning spaces around ",Messenger.tp("b",blockpos));
            }
            notifyCommandListener(sender, this, "Spawn spaces:");
            notifyCommandListener(sender, this, String.format("  potential in-liquid: %d",res.liquid));
            notifyCommandListener(sender, this, String.format("  potential on-ground: %d",res.ground));
            if (entityliving != null)
            {
                notifyCommandListener(sender, this, String.format("  %s: %d",entityliving.getDisplayName().getUnformattedText(),res.specific));
                if (sender instanceof EntityPlayer)
                {
                    res.samples.forEach(bp -> Messenger.m((EntityPlayer)sender, "w   ", Messenger.tp("w", bp)));
                }
                else
                {
                    res.samples.forEach(bp -> notifyCommandListener(sender, this, String.format("    [ %d, %d, %d ]", bp.getX(),bp.getY(),bp.getZ())));
                }
                entityliving.setDead();
            }
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, EntityList.getEntityNameList());
        }
        else
        {
            return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : Collections.emptyList();
        }
    }
}

