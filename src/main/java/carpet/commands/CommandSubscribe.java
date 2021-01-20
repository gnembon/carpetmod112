package carpet.commands;

import net.minecraft.command.CommandSource;

public class CommandSubscribe extends CommandLog {

    private final String USAGE = "/subscribe <subscribeName> [?option]";

    @Override
    public String method_29277() {
        return "subscribe";
    }

    @Override
    public String method_29275(CommandSource sender) {
        return USAGE;
    }
}
