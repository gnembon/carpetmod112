package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.function.Supplier;

public final class DebugLogHelper {
    private DebugLogHelper() {}

    public static void invisDebug(Supplier<String> info) {
        invisDebug(info, false);
    }

    public static void invisDebug(Supplier<String> info, boolean stackTrace) {
        if (!LoggerRegistry.__invisDebug) return;
        if (stackTrace) {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            StringBuilder s = new StringBuilder();
            for(int i = 2; i < trace.length; i++){
                s.append(trace[i].toString()).append('\n');
            }
            invisDebug(new TextComponentString(info.get() + " " + s.toString()));
        } else {
            invisDebug(new TextComponentString(info.get()));
        }
    }

    public static void invisDebug(ITextComponent ...texts) {
        if (!LoggerRegistry.__invisDebug) return;
        LoggerRegistry.getLogger("invisDebug").log(()-> texts);
    }
}
