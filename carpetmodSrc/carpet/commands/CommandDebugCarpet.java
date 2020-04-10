package carpet.commands;

import javax.annotation.Nullable;

import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

public class CommandDebugCarpet extends CommandCarpetBase {

    @Override
    public String getName() {
        return "debugCarpet";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: debugCarpet <debug option>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if("tracker".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((WorldServer)sender.getEntityWorld()).getEntityTracker().getEntries()){
                sender.sendMessage(Messenger.s(sender, e.getTrackedEntity().toString()));
            }
        }
        if("trackedToMe".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((WorldServer)sender.getEntityWorld()).getEntityTracker().getEntries()){
                if(e.isVisibleTo((EntityPlayerMP) sender)){
                    sender.sendMessage(Messenger.s(sender, e.getTrackedEntity().toString()));
                }
            }
        }
        if("entitys".equalsIgnoreCase(args[0])) {
            for(Entity e : sender.getEntityWorld().loadedEntityList){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if(args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "tracker", "entitys", "trackedToMe");
        }
        return Collections.<String>emptyList();
    }
}
