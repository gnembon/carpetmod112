package accuratetimer;

import java.io.File;
import java.util.Locale;

public class AccurateTimer {
    static {
        // Source available at: https://github.com/Earthcomputer/rdtsc_native
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String libName;
        if (!osName.contains("darwin") && osName.contains("win")) {
            libName = "native\\rdtsc_native.dll";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            libName = "native/librdtsc_native.so";
        } else { // macos
            libName = "native/librdtsc_native.jnilib";
        }
        for (File dir = new File(".").getAbsoluteFile(); dir != null; dir = dir.getParentFile()) {
            File lib = new File(dir, libName);
            if (lib.exists()) {
                System.load(lib.getAbsolutePath());
                break;
            }
        }

        initialize();
    }

    public static void load() {
        // load the class
    }

    private static native void initialize();

    public static native long rdtsc();

    public static native long delta(long start, long end);
}
