package carpet.mixin.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandTime;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(CommandTime.class)
public abstract class CommandTimeMixin extends CommandBase {
    @Inject(method = "execute", at = @At(value = "NEW", target = "net/minecraft/command/WrongUsageException"), cancellable = true)
    private void queryServerTime(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci) {
        if (args.length >= 2 && "query".equals(args[0]) && "servertime".equals(args[1])) {
            int time = (int) (sender.getEntityWorld().getTotalWorldTime() % Integer.MAX_VALUE);
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, time);
            notifyCommandListener(sender, this, "commands.time.query", time);
            ci.cancel();
        }
    }

    @Redirect(method = "getTabCompletions", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/CommandTime;getListOfStringsMatchingLastWord([Ljava/lang/String;[Ljava/lang/String;)Ljava/util/List;", ordinal = 2))
    private List<String> tabCompleteServerTime(String[] args, String... possibilities) {
        String[] poss = Arrays.copyOfRange(possibilities, 0, possibilities.length + 1);
        poss[poss.length - 1] = "servertime";
        return getListOfStringsMatchingLastWord(args, poss);
    }
}
