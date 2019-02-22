package carpet.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import carpet.CarpetSettings;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandAutosave extends CommandCarpetBase {

	@Override
	public String getName() {
		return "autosave";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: autosave info | autosave detect <range-start> <range-end> <quiet t| run <command>>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!command_enabled("commandAutosave", sender)) return;
		
		if(args.length < 1)
		{
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
		
		int gametick = server.getTickCounter();
		
		int afterAutosave = gametick%900;
		
		if("info".equalsIgnoreCase(args[0])) {
			int previous = afterAutosave;
			int interval = gametick/900;
			if(gametick != 0 && previous == 0) {
				previous = 900;
				interval--;
			}
			int next = 900 - previous;
			int beforeAutosave = 900-previous;
			notifyCommandListener(sender, this, String.format("Autosave (interval %d) %d gameticks ago - in %d ticks", interval, previous, next));
		}
		else if("detect".equalsIgnoreCase(args[0])) {
			if(args.length < 3) {
				throw new WrongUsageException(getUsage(sender), new Object[0]);
			}
			
			int start = this.parseInt(args[1]);
			int end = this.parseInt(args[2]);
			boolean quiet = false;
			String run = null;

			if(args.length == 4) {
				if("quiet".equalsIgnoreCase(args[3])) {
					quiet = true;
				}
				else {
					throw new WrongUsageException(getUsage(sender), new Object[0]);
				}
			}
			else if(args.length > 4) {
				if("run".equals(args[3])) {
					run = buildString(args,4);
					quiet = true;
				}
				else {
					throw new WrongUsageException(getUsage(sender), new Object[0]);
				}
			}
			
			start = (start % 900 + 900) % 900;
			end = (end % 900 + 900) % 900;
			boolean innerInterval = (start <= end);
			boolean pass = innerInterval ?
					(start <= afterAutosave) && (afterAutosave <= end):
					(start <= afterAutosave) || (afterAutosave <= end);
			
			if(pass) {
				if(!quiet) {
					notifyCommandListener(sender, this, String.format("gametick %d in interval %d %d",afterAutosave, start, end));
				}
				if(run != null) {
					server.getCommandManager().executeCommand(sender, run);
				}
			}
			else {
				throw new CommandException(String.format("gametick %d not in interval %d %d",afterAutosave, start, end));
			}
		}
		else {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}
	
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandAutosave)
        {
            return Collections.<String>emptyList();
        }
        if(args.length == 1) {
        	return getListOfStringsMatchingLastWord(args, "info", "detect");
        }
        if(args.length == 4) {
        	return getListOfStringsMatchingLastWord(args, "run", "quiet");
        }
		return Collections.<String>emptyList();
    }
}
