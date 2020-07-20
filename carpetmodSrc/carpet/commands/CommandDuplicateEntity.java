package carpet.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandDuplicateEntity extends CommandBase {
    @Override
    public String getName() {
        return "dupeentity";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/dupeentity <entity> <x> <y> <z>";
    }

    @SuppressWarnings("NoTranslation")
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 4) {
            throw new WrongUsageException(getUsage(sender));
        }

        Entity entity = EntitySelector.matchOneEntity(sender, args[0], Entity.class);
        if (entity == null) {
            throw new CommandException("Entity not found");
        } else if (entity instanceof EntityPlayer) {
            throw new CommandException("Cannot duplicate players");
        }
        Vec3d senderPos = sender.getPositionVector();
        double x = parseDouble(senderPos.x, args[1], true);
        double y = parseDouble(senderPos.y, args[2], false);
        double z = parseDouble(senderPos.z, args[3], true);

        NBTTagCompound nbt = new NBTTagCompound();
        entity.writeToNBTOptional(nbt);
        Entity newEntity = AnvilChunkLoader.readWorldEntityPos(nbt, sender.getEntityWorld(), x, y, z, true);
        if (newEntity != null && newEntity != entity) {
            notifyCommandListener(sender, this, "Successfully duplicated entity");
        } else {
            throw new CommandException("Failed to duplicate entity");
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, EntityList.getEntityNameList());
        } else {
            return args.length > 1 && args.length <= 4 ? getTabCompletionCoordinate(args, 1, targetPos) : Collections.emptyList();
        }
    }
}
