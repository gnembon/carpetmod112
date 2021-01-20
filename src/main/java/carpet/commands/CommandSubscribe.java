package carpet.commands;

import net.minecraft.class_2010;

public class CommandSubscribe extends CommandLog {

    private final String USAGE = "/subscribe <subscribeName> [?option]";

    @Override
    public String method_29277() {
        return "subscribe";
    }

    @Override
    public String method_29275(class_2010 sender) {
        return USAGE;
    }
}
