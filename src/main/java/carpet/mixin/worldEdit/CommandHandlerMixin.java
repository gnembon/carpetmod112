package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.class_2002;
import net.minecraft.class_2010;
import net.minecraft.class_5607;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(class_2002.class)
public class CommandHandlerMixin {
    private final ThreadLocal<Boolean> weSession = new ThreadLocal<>();

    @Redirect(method = "method_29374", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object startEditSession(Map<String, class_5607> commandMap, Object commandName, class_2010 sender) {
        //noinspection SuspiciousMethodCalls
        class_5607 command = commandMap.get(commandName);
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        boolean nonWorldEditCommand = command != null && !command.getClass().getName().startsWith("carpet.worldedit.");
        boolean weSession = nonWorldEditCommand && WorldEditBridge.worldEditEnabled();
        this.weSession.set(weSession);
        if (weSession) WorldEditBridge.startEditSession(worldEditPlayer);
        return command;
    }

    @Inject(method = "method_29374", at = @At(value = "CONSTANT", args = "intValue=-1"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCommand(class_2010 sender, String rawCommand, CallbackInfoReturnable<Integer> cir, String[] args, String commandName, class_5607 command) {
        WorldEditBridge.onCommand(command, sender, args);
    }

    // Not as good as a finally block, but should do the job since CommandExceptions are already handled
    @Inject(method = "method_29374", at = @At("RETURN"))
    private void endEditSession(class_2010 sender, String rawCommand, CallbackInfoReturnable<Integer> cir) {
        if (!weSession.get()) return;
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.finishEditSession(worldEditPlayer);
        weSession.set(false);
    }
}
