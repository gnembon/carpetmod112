package carpet.worldedit;

import java.util.Arrays;
import java.util.List;

import com.sk89q.worldedit.util.command.CommandMapping;

import carpet.commands.CommandCarpetBase;
import net.minecraft.class_5607;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

class CommandWrapper extends CommandCarpetBase {
    private CommandMapping command;

    protected CommandWrapper(CommandMapping command) {
        this.command = command;
    }

    @Override
    public String method_29277() {
        return command.getPrimaryAlias();
    }

    @Override
    public List<String> method_29274() {
        return Arrays.asList(command.getAllAliases());
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource var1, String[] var2) {}

    @Override
    public String method_29275(CommandSource sender) {
        return "/" + command.getPrimaryAlias() + " " + command.getDescription().getUsage();
    }

    @Override
    public int method_28700() {
        return 0;
    }

    @Override
    public boolean method_29271(MinecraftServer server, CommandSource sender) {
        return command_enabled("worldEdit", sender); // Will send an extra vanilla permission message but that's the best we can do
    }

    @Override
    public int compareTo(class_5607 o) {
        return super.compareTo(o);
    }
}
