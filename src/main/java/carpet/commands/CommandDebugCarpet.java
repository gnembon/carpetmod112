package carpet.commands;

import javax.annotation.Nullable;

import carpet.mixin.accessors.EntityTrackerAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.utils.Messenger;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import java.util.Collections;
import java.util.List;

public class CommandDebugCarpet extends CommandCarpetBase {
    @Override
    public String method_29277() {
        return "debugCarpet";
    }

    @Override
    public String method_29275(CommandSource sender) {
        return "Usage: debugCarpet <debug option>";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if("tracker".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((EntityTrackerAccessor) ((ServerWorld) sender.getEntityWorld()).getEntityTracker()).getEntries()){
                sender.sendSystemMessage(Messenger.s(sender, e.method_33550().toString()));
            }
        }
        if("trackedToMe".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((EntityTrackerAccessor) ((ServerWorld) sender.getEntityWorld()).getEntityTracker()).getEntries()){
                if(e.method_33555((ServerPlayerEntity) sender)){
                    sender.sendSystemMessage(Messenger.s(sender, e.method_33550().toString()));
                }
            }
        }
        if("entitys".equalsIgnoreCase(args[0])) {
            for(Entity e : sender.getEntityWorld().field_23572){
                sender.sendSystemMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("tileEntitys1".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.getEntityWorld().blockEntities){
                sender.sendSystemMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("tileEntitys2".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.getEntityWorld().tickingBlockEntities){
                sender.sendSystemMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("playerEntities".equalsIgnoreCase(args[0])) {
            for(PlayerEntity e : sender.getEntityWorld().field_23576){
                sender.sendSystemMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("pendingTickListEntriesTreeSet".equalsIgnoreCase(args[0])) {
            for(ScheduledTick e : ((ServerWorldAccessor)sender.getEntityWorld()).getPendingTickListEntriesTreeSet()){
                sender.sendSystemMessage(Messenger.s(sender, e.toString()));
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if(args.length == 1) {
            return method_28732(args, "tracker", "entitys", "trackedToMe", "tileEntitys1", "tileEntitys2", "playerEntities", "pendingTickListEntriesTreeSet");
        }
        return Collections.emptyList();
    }
}
