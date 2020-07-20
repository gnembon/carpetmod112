package carpet.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class OrderCommand extends CommandBase {
    @Override
    public String getName() {
        return "order";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/order";
    }

    @SuppressWarnings("NoTranslation")
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        List<Entity> entities = new ArrayList<>();
        Chunk chunk = sender.getEntityWorld().getChunk(sender.getPosition());
        for (ClassInheritanceMultiMap<Entity> list : chunk.getEntityLists()) {
            for (Entity entity : list) {
                //noinspection UseBulkOperation
                entities.add(entity);
            }
        }
        if (entities.isEmpty()) {
            throw new CommandException("No entities");
        } else {
            notifyCommandListener(sender, this, entities.size() + " entities in chunk");
            for (Entity entity : entities) {
                sender.sendMessage(entity.getDisplayName());
            }
        }
    }
}
