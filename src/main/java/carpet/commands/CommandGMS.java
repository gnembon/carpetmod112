package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.extensions.CameraPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.class_6182;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffect;

public class CommandGMS extends CommandCarpetBase
{
    @Override
    public String method_29277()
    {
        return "s";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return "commands.gamemode.usage";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandCameramode", sender)) return;
        if (args.length > 0)
        {
            throw new class_6182(method_29275(sender));
        }
        else
        {
            ServerPlayerEntity entityplayer = method_28708(sender);
            setPlayerToSurvival(server, entityplayer,false);
        }
    }

    public static void setPlayerToSurvival(MinecraftServer server, ServerPlayerEntity entityplayer, boolean alwaysPutPlayerInSurvival) {
        GameMode gametype = server.getDefaultGameMode();
        if(entityplayer.interactionManager.getGameMode() != GameMode.SURVIVAL) {
            DebugLogHelper.invisDebug(() -> "s1: " + entityplayer.world.field_23572.contains(entityplayer));
            if(((CameraPlayer) entityplayer).moveToStoredCameraData() &&  !alwaysPutPlayerInSurvival) {
                DebugLogHelper.invisDebug(() -> "s7: " + entityplayer.world.field_23572.contains(entityplayer));
                return;
            }
            entityplayer.fallDistance = 0;
            DebugLogHelper.invisDebug(() -> "s5: " + entityplayer.world.field_23572.contains(entityplayer));
            if(gametype != GameMode.SPECTATOR) {
                entityplayer.setGameMode(gametype);
            } else {
                entityplayer.setGameMode(GameMode.SURVIVAL);
            }
            if(!((CameraPlayer) entityplayer).hadNightvision()) entityplayer.removeStatusEffect(StatusEffect.fromId("night_vision"));
            DebugLogHelper.invisDebug(() -> "s6: " + entityplayer.world.field_23572.contains(entityplayer));
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.emptyList();
    }
}
