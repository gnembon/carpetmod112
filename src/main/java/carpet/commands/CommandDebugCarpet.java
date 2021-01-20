package carpet.commands;

import javax.annotation.Nullable;

import carpet.mixin.accessors.EntityTrackerAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.utils.Messenger;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.class_2010;
import net.minecraft.class_6175;
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
    public String method_29275(class_2010 sender) {
        return "Usage: debugCarpet <debug option>";
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175 {
        if("tracker".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((EntityTrackerAccessor) ((ServerWorld) sender.method_29608()).method_33488()).getEntries()){
                sender.sendMessage(Messenger.s(sender, e.method_33550().toString()));
            }
        }
        if("trackedToMe".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((EntityTrackerAccessor) ((ServerWorld) sender.method_29608()).method_33488()).getEntries()){
                if(e.method_33555((ServerPlayerEntity) sender)){
                    sender.sendMessage(Messenger.s(sender, e.method_33550().toString()));
                }
            }
        }
        if("entitys".equalsIgnoreCase(args[0])) {
            for(Entity e : sender.method_29608().field_23572){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("tileEntitys1".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.method_29608().blockEntities){
                sender.sendMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("tileEntitys2".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.method_29608().tickingBlockEntities){
                sender.sendMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("playerEntities".equalsIgnoreCase(args[0])) {
            for(PlayerEntity e : sender.method_29608().field_23576){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("pendingTickListEntriesTreeSet".equalsIgnoreCase(args[0])) {
            for(ScheduledTick e : ((ServerWorldAccessor)sender.method_29608()).getPendingTickListEntriesTreeSet()){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args, @Nullable BlockPos pos)
    {
        if(args.length == 1) {
            return method_28732(args, "tracker", "entitys", "trackedToMe", "tileEntitys1", "tileEntitys2", "playerEntities", "pendingTickListEntriesTreeSet");
        }
        return Collections.emptyList();
    }
}
