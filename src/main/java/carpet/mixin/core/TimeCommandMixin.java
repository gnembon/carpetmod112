package carpet.mixin.core;

import net.minecraft.class_1999;
import net.minecraft.class_2010;
import net.minecraft.class_2014;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin extends class_1999 {
    @Inject(method = "method_29272", at = @At(value = "NEW", target = "net/minecraft/class_6182"), cancellable = true)
    private void queryServerTime(MinecraftServer server, class_2010 sender, String[] args, CallbackInfo ci) {
        if (args.length >= 2 && "query".equals(args[0]) && "servertime".equals(args[1])) {
            int time = (int) (sender.method_29608().getTime() % Integer.MAX_VALUE);
            sender.method_29604(class_2014.class_5737.field_28183, time);
            method_28710(sender, this, "commands.time.query", time);
            ci.cancel();
        }
    }

    @Redirect(method = "method_29273", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/TimeCommand;method_28732([Ljava/lang/String;[Ljava/lang/String;)Ljava/util/List;", ordinal = 2))
    private List<String> tabCompleteServerTime(String[] args, String... possibilities) {
        String[] poss = Arrays.copyOfRange(possibilities, 0, possibilities.length + 1);
        poss[poss.length - 1] = "servertime";
        return method_28732(args, poss);
    }
}
