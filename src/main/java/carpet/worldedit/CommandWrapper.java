package carpet.worldedit;

import java.util.Arrays;
import java.util.List;

import com.sk89q.worldedit.util.command.CommandMapping;

import carpet.commands.CommandCarpetBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

class CommandWrapper extends CommandCarpetBase {
    private CommandMapping command;

    protected CommandWrapper(CommandMapping command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getPrimaryAlias();
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(command.getAllAliases());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {}

    @Override
    public String getUsage(ICommandSender icommandsender) {
        return "/" + command.getPrimaryAlias() + " " + command.getDescription().getUsage();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return command_enabled("worldEdit", sender); // Will send an extra vanilla permission message but that's the best we can do
    }

    @Override
    public int compareTo(ICommand o) {
        return super.compareTo((ICommand) o);
    }
}
