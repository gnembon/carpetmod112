package carpet.commands;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public class CommandAstral extends CommandCarpetBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "astral";
    }

    @Override
    public String getName() {
        return "astral";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: astral <clickType> <clickSpeed>";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 0;
    }

    /**
     * Callback for when the command is executed
     *
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args   The arguments that were passed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandAstral", sender)) {
            throw new WrongUsageException("Enable commandAstral to use this command.");
        }
        if (!(sender instanceof EntityPlayerMP)) {
            throw new WrongUsageException("Only players can use this command.");
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;
        int leftSpeed = 0;
        int rightSpeed = 0;

        if (args.length >= 1) {
            String type = args[0];
            if (type.equals("leftClick")) {
                if (args.length >= 2) {
                    leftSpeed = parseInt(args[1]);
                } else {
                    leftSpeed = 20;
                }
            } else if (type.equals("rightClick")) {
                if (args.length >= 2) {
                    rightSpeed = parseInt(args[2]);
                } else {
                    rightSpeed = 4;
                }
            } else if (type.equals("leftAndRight")) {
                if (args.length == 2) {
                    leftSpeed = rightSpeed = parseInt(args[2]);
                } else if (args.length >= 3) {
                    leftSpeed = parseInt(args[2]);
                    rightSpeed = parseInt(args[3]);
                } else {
                    leftSpeed = 20;
                    rightSpeed = 4;
                }
            } else if (type.equals("kick") && args.length == 2) {
                String playerName = args[1];
                EntityPlayerMP kick = server.getPlayerList().getPlayerByUsername(playerName);
                if (!(kick instanceof EntityPlayerMPFake)) {
                    throw new WrongUsageException("/astral kick can only be used on astral or fake players");
                }
                kick.onKillCommand();
                return;
            }
        }

        EntityPlayerMP astral = EntityPlayerMPFake.createAstral(server, player);
        if (leftSpeed != 0) astral.actionPack.setAttack(leftSpeed, 50);
        if (rightSpeed != 0) astral.actionPack.setUse(rightSpeed, 50);
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if(args.length > 1)
            System.out.println("testing :" + args[0]+":1");
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[]{"leftClick", "rightClick", "leftAndRight", "kick"});
        } else if (args.length == 2 && "leftClick".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, new String[]{"20"});
        } else if (args.length == 2 && "rightClick".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, new String[]{"4"});
        } else if (args.length > 1 && "leftAndRight".equalsIgnoreCase(args[0])) {
            if (args.length == 2) {
                return getListOfStringsMatchingLastWord(args, new String[]{"20"});
            }
            else if (args.length == 3) {
                return getListOfStringsMatchingLastWord(args, new String[]{"4"});
            }
        } else if (args.length > 1 && "kick".equalsIgnoreCase(args[0])) {
            Set<String> players = new HashSet<>(Arrays.asList(server.getOnlinePlayerNames()));
            return getListOfStringsMatchingLastWord(args, players.toArray(new String[0]));
        }
        return Collections.<String>emptyList();
    }
}