package carpet;

import net.minecraft.launchwrapper.Launch;

public class Main {
    public static String[] args;

    public static void main(String[] args) {
        Main.args = args;
        Launch.main(new String[]{"--tweakClass", "carpet.launch.ServerTweaker"});
    }
}
