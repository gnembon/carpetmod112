package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public final class DebugLogHelper {
    private DebugLogHelper() {}

    public static void invisDebug(Supplier<String> info) {
        if(LoggerRegistry.__invisDebug) {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            StringBuilder s = new StringBuilder();
            for(int i = 2; i < trace.length; i++){
                s.append(trace[i].toString()).append('\n');
            }
            final String ss = s.toString();
            LoggerRegistry.getLogger("invisDebug").log(()-> new ITextComponent[]{
                Messenger.s(null, info.get() + " " + ss)
            });
        }
    }
}
