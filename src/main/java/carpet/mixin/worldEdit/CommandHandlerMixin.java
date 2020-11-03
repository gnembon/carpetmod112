package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(CommandHandler.class)
public class CommandHandlerMixin {
    private final ThreadLocal<Boolean> weSession = new ThreadLocal<>();

    @Redirect(method = "executeCommand", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object startEditSession(Map<String, ICommand> commandMap, Object commandName, ICommandSender sender) {
        //noinspection SuspiciousMethodCalls
        ICommand command = commandMap.get(commandName);
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        boolean nonWorldEditCommand = command != null && !command.getClass().getName().startsWith("carpet.worldedit.");
        boolean weSession = nonWorldEditCommand && WorldEditBridge.worldEditEnabled();
        this.weSession.set(weSession);
        if (weSession) WorldEditBridge.startEditSession(worldEditPlayer);
        return command;
    }

    @Inject(method = "executeCommand", at = @At(value = "CONSTANT", args = "intValue=-1"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCommand(ICommandSender sender, String rawCommand, CallbackInfoReturnable<Integer> cir, String[] args, String commandName, ICommand command) {
        WorldEditBridge.onCommand(command, sender, args);
    }

    // Not as good as a finally block, but should do the job since CommandExceptions are already handled
    @Inject(method = "executeCommand", at = @At("RETURN"))
    private void endEditSession(ICommandSender sender, String rawCommand, CallbackInfoReturnable<Integer> cir) {
        if (!weSession.get()) return;
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.finishEditSession(worldEditPlayer);
        weSession.set(false);
    }
}
