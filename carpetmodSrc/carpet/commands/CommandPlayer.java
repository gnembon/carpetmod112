package carpet.commands;

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

public class CommandPlayer extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "player";
    }


    /**
     * Gets the usage string for the command.
     *
     * @param sender The ICommandSender who is requesting usage details
     */
    public String getUsage(ICommandSender sender)
    {
        return "player <spawn|kill|stop|drop|swapHands|mount|dismount> <player_name>  OR /player <use|attack|jump> <player_name> <once|continuous|interval.. ticks>";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandPlayer", sender)) return;
        if (args.length < 2)
        {
            throw new WrongUsageException("player <x> action");
        }
        String playerName = args[0];
        String action = args[1];
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
        if (sender instanceof EntityPlayer)
        {
            EntityPlayer sendingPlayer = getCommandSenderAsPlayer(sender);
            if (!(server.getPlayerList().canSendCommands(sendingPlayer.getGameProfile())))
            {
                if (!(sendingPlayer == player || player == null || player instanceof EntityPlayerMPFake))
                {
                    throw new WrongUsageException("Non OP players can't control other players");
                }
            }
        }
        if (player == null && !action.equalsIgnoreCase("spawn"))
        {
            throw new WrongUsageException("player doesn't exist");
        }
        if ("use".equalsIgnoreCase(action) || "attack".equalsIgnoreCase(action) || "jump".equalsIgnoreCase(action))
        {
            String option = "once";
            int interval = 0;
            if (args.length > 2)
            {
                option = args[2];
                if (args.length > 3 && option.equalsIgnoreCase("interval"))
                {
                    interval = parseInt(args[3],2,72000);
                }
            }
            if (action.equalsIgnoreCase("use"))
            {
                if (option.equalsIgnoreCase("once"))
                    player.actionPack.useOnce();
                if (option.equalsIgnoreCase("continuous"))
                    player.actionPack.setUseForever();
                if (option.equalsIgnoreCase("interval") && interval > 1)
                    player.actionPack.setUse(interval, 0);
            }
            if (action.equalsIgnoreCase("attack"))
            {
                if (option.equalsIgnoreCase("once"))
                    player.actionPack.attackOnce();
                if (option.equalsIgnoreCase("continuous"))
                    player.actionPack.setAttackForever();
                if (option.equalsIgnoreCase("interval") && interval > 1)
                    player.actionPack.setAttack(interval, 0);
            }
            if (action.equalsIgnoreCase("jump"))
            {
                if (option.equalsIgnoreCase("once"))
                    player.actionPack.jumpOnce();
                if (option.equalsIgnoreCase("continuous"))
                    player.actionPack.setJumpForever();
                if (option.equalsIgnoreCase("interval") && interval > 1)
                    player.actionPack.setJump(interval, 0);
            }
            return;
        }
        if ("stop".equalsIgnoreCase(action))
        {
            player.actionPack.stop();
            return;
        }
        if ("drop".equalsIgnoreCase(action))
        {
            player.actionPack.dropItem();
            return;
        }
        if ("swapHands".equalsIgnoreCase(action))
        {
            player.actionPack.swapHands();
            return;
        }
        if ("spawn".equalsIgnoreCase(action))
        {
            if (player != null)
            {
                throw new WrongUsageException("player "+playerName+" already exists");
            }
            if (playerName.length() < 3 || playerName.length() > 16)
            {
                throw new WrongUsageException("player names can only be 3 to 16 chars long");
            }
            if (isWhitelistedPlayer(server, playerName) && !sender.canUseCommand(2, "gamemode")) {
                throw new CommandException("You are not allowed to spawn a whitelisted player");
            }
            Vec3d vec3d = sender.getPositionVector();
            double d0 = vec3d.x;
            double d1 = vec3d.y;
            double d2 = vec3d.z;
            double yaw = 0.0D;
            double pitch = 0.0D;
            World world = sender.getEntityWorld();
            int dimension = world.provider.getDimensionType().getId();
            int gamemode = server.getGameType().getID();

            if (sender instanceof EntityPlayerMP)
            {
                EntityPlayerMP entity = getCommandSenderAsPlayer(sender);
                yaw = (double)entity.rotationYaw;
                pitch = (double)entity.rotationPitch;
                gamemode = entity.interactionManager.getGameType().getID();
            }
            if (args.length >= 5)
            {
                d0 = parseCoordinate(d0, args[2], true).getResult();
                d1 = parseCoordinate(d1, args[3], -4096, 4096, false).getResult();
                d2 = parseCoordinate(d2, args[4], true).getResult();
                yaw = parseCoordinate(yaw, args.length > 5 ? args[5] : "~", false).getResult();
                pitch = parseCoordinate(pitch, args.length > 6 ? args[6] : "~", false).getResult();
            }
            if (args.length >= 8)
            {
                String dimension_string = args[7];
                dimension = 0;
                if ("nether".equalsIgnoreCase(dimension_string))
                {
                    dimension = -1;
                }
                if ("end".equalsIgnoreCase(dimension_string))
                {
                    dimension = 1;
                }
            }
            if (args.length >= 9)
            {
                gamemode = parseInt(args[8],0,3);
                if (gamemode == 1 && !sender.canUseCommand(2, "gamemode")) {
                    throw new CommandException("You are not allowed to spawn a creative player");
                }
            }
            EntityPlayerMPFake.createFake(playerName, server, d0, d1, d2, yaw, pitch, dimension, gamemode );
            return;
        }
        if ("kill".equalsIgnoreCase(action))
        {
            if (!(player instanceof EntityPlayerMPFake))
            {
                throw new WrongUsageException("use /kill or /kick on regular players");
            }
            player.onKillCommand();
            return;
        }
        if ("shadow".equalsIgnoreCase(action))
        {
            if (player instanceof EntityPlayerMPFake)
            {
                throw new WrongUsageException("cannot shadow server side players");
            }
            EntityPlayerMPFake.createShadow(server, player);
            return;
        }
        if ("mount".equalsIgnoreCase(action))
        {
            player.actionPack.mount();
            return;
        }
        if ("dismount".equalsIgnoreCase(action))
        {
            player.actionPack.dismount();
            return;
        }
        //FP only
        if (action.matches("^(?:move|sneak|sprint|look)$"))
        {
            if (player != null && !(player instanceof EntityPlayerMPFake))
                throw new WrongUsageException(action+" action could only be run on existing fake players");

            if ("move".equalsIgnoreCase(action))
            {
                if (args.length < 3)
                    throw new WrongUsageException("/player "+playerName+" go <forward|backward|left|right>");
                String where = args[2];
                if ("forward".equalsIgnoreCase(where))
                {
                    player.actionPack.setForward(1.0F);
                    return;
                }
                if ("backward".equalsIgnoreCase(where))
                {
                    player.actionPack.setForward(-1.0F);
                    return;
                }
                if ("left".equalsIgnoreCase(where))
                {
                    player.actionPack.setStrafing(-1.0F);
                    return;
                }
                if ("right".equalsIgnoreCase(where))
                {
                    player.actionPack.setStrafing(1.0F);
                    return;
                }
                throw new WrongUsageException("/player "+playerName+" go <forward|backward|left|right>");
            }
            if ("sneak".equalsIgnoreCase(action))
            {
                player.actionPack.setSneaking(true);
                return;
            }
            if ("sprint".equalsIgnoreCase(action))
            {
                player.actionPack.setSprinting(true);
                return;
            }
            if ("look".equalsIgnoreCase(action))
            {
                if (args.length < 3)
                    throw new WrongUsageException("/player "+playerName+" look <left|right|north|south|east|west|up|down| yaw .. pitch>");
                if (args[2].charAt(0)>='A' && args[2].charAt(0)<='z')
                {
                    if(!player.actionPack.look(args[2].toLowerCase()))
                    {
                        throw new WrongUsageException("look direction is north, south, east, west, up or down");
                    }
                }
                else if (args.length > 3)
                {
                    float yaw = (float) parseCoordinate(player.rotationYaw, args[2], false).getResult();
                    float pitch = (float) parseCoordinate(player.rotationPitch, args[3], false).getResult();
                    player.actionPack.look(yaw,pitch);
                }
                else
                {
                    throw new WrongUsageException("/player "+playerName+" look <north|south|east|west|up|down| yaw .. pitch>");
                }
                return;
            }
        }
        throw new WrongUsageException("unknown action: "+action);
    }

    private boolean isWhitelistedPlayer(MinecraftServer server, String playerName) {
        for(String s : server.getPlayerList().getWhitelistedPlayerNames()){
            if(s.toLowerCase().equals(playerName.toLowerCase())) return true;
        }
        return false;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (!CarpetSettings.commandPlayer)
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            Set<String> players = new HashSet<>(Arrays.asList(server.getOnlinePlayerNames()));
            players.add("Steve");
            players.add("Alex");
            return getListOfStringsMatchingLastWord(args, players.toArray(new String[0]));
        }
        if (args.length == 2)
        {
            //currently for all, needs to be restricted for Fake plaeyrs
            return getListOfStringsMatchingLastWord(args,
                    "spawn","kill","attack","use","jump","stop","shadow",
                    "swapHands","drop","mount","dismount",
                    "move","sneak","sprint","look");
        }
        if (args.length == 3 && (args[1].matches("^(?:use|attack|jump)$")))
        {
            //currently for all, needs to be restricted for Fake plaeyrs
            return getListOfStringsMatchingLastWord(args, "once","continuous","interval");
        }
        if (args.length == 4 && (args[1].equalsIgnoreCase("interval")))
        {
            //currently for all, needs to be restricted for Fake plaeyrs
            return getListOfStringsMatchingLastWord(args, "20");
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("move")))
        {
            return getListOfStringsMatchingLastWord(args, "left", "right","forward","backward");
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("look")))
        {
            return getListOfStringsMatchingLastWord(args, "left", "right","north","south","east","west","up","down");
        }
        if (args.length > 2 && (args[1].equalsIgnoreCase("spawn") ))
        {
            if (args.length <= 5)
            {
                return getTabCompletionCoordinate(args, 2, targetPos);
            }
            else if (args.length <= 7)
            {
                return getListOfStringsMatchingLastWord(args, "0.0");
            }
            else if (args.length == 8)
            {
                return getListOfStringsMatchingLastWord(args, "overworld", "end", "nether");
            }
            else if (args.length == 9)
            {
                return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3");
            }
        }
        return Collections.emptyList();
    }
}
