package carpet.commands;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.class_6175;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandRemoveEntity extends KillCommand {
    @Override
    public String method_29277()
    {
        return "removeEntity";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
    {
        if (args.length == 0)
        {
            PlayerEntity entityplayer = method_28708(sender);
            entityplayer.kill();
            method_28710(sender, this, "commands.kill.successful", entityplayer.getDisplayName());
        }
        else
        {
            Entity entity = method_28743(server, sender, args[0]);
            entity.remove();

            if (!(entity instanceof ServerPlayerEntity))
            {
                ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
                WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getEntityWorld(), entity);
            }

            method_28710(sender, this, "commands.kill.successful", entity.getDisplayName());
        }
    }

}
