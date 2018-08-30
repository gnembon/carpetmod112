package carpet.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.utils.Chunklogger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandDebugChunks extends CommandCarpetBase {

	@Override
	public String getName() {
		return "debugchunks";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "Usage: debugchunks start <name> x1 z1 x2 z2 | debugchunks stop <name>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandDebugChunks", sender))
            return;
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        if("start".equalsIgnoreCase(args[0])) {
        	
            if (args.length < 6) {
                throw new WrongUsageException(getUsage(sender));
            }
        	
        	if(!args[0].matches("^[a-zA-Z0-9_]*$")) {
        		throw new CommandException("Bad characters in name");
        	}
        	
        	String name = args[1];
        	
        	int x1 = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[2], false).getResult());
        	int z1 = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[3], false).getResult());
        	int x2 = (int) Math.round(parseCoordinate(sender.getPosition().getX(), args[4], false).getResult());
        	int z2 = (int) Math.round(parseCoordinate(sender.getPosition().getZ(), args[5], false).getResult());
        
        	int minX = Math.min(x1, x2) >> 4;
        	int maxX = Math.max(x1, x2) >> 4;
        	int minZ = Math.min(z1, z2) >> 4;
        	int maxZ = Math.max(z1, z2) >> 4;

        	World w = sender.getEntityWorld();
        	Chunklogger cl = new Chunklogger(sender.getEntityWorld(),minX,minZ,maxX,maxZ,name);
        	notifyCommandListener(sender, this,"Started logging chunk loading to " + args[1]);
        }
        else if("stop".equalsIgnoreCase(args[0])) {
        	World w = sender.getEntityWorld();
        	w.chunkloggers.remove(args[1]);
        	notifyCommandListener(sender, this,"Stopped logging chunk loading to " + args[1]);
        }
        else {
        	throw new WrongUsageException(getUsage(sender));
        }
	}
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos){
        if (!CarpetSettings.getBool("commandDebugChunks"))
        {
            return Collections.<String>emptyList();
        }
        if(args.length == 0) {
        	return Collections.<String>emptyList();
        }
        if(args.length == 1) {
        	return Arrays.asList("start","stop");
        }
        if(args.length == 2 && "stop".equalsIgnoreCase(args[0])) {
        	return new ArrayList<String>(sender.getEntityWorld().chunkloggers.keySet());
        }
        if(args.length == 2 && "start".equalsIgnoreCase(args[0])) {
            return Collections.<String>emptyList();
        }
        if(args.length >= 3 && "start".equalsIgnoreCase(args[0])) {
            if (args.length == 3 || args.length == 5)
            {
                if (targetPos == null)
                    return getListOfStringsMatchingLastWord(args, "~");
                else
                    return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getX()));
            }
            else if (args.length == 4 || args.length == 6)
            {
                if (targetPos == null)
                    return getListOfStringsMatchingLastWord(args, "~");
                else
                    return getListOfStringsMatchingLastWord(args, String.valueOf(targetPos.getZ()));
            }
        }
        return Collections.<String>emptyList();
	}
}


