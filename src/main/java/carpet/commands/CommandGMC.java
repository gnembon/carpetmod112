package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import carpet.mixin.accessors.EntityAccessor;
import carpet.utils.extensions.CameraPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import net.minecraft.class_6182;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.class_6175;

public class CommandGMC extends CommandCarpetBase
{
    @Override
    public String method_29277()
    {
        return "c";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return "commands.gamemode.usage";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175 {
        if (!command_enabled("commandCameramode", sender)) return;
        if (args.length > 0)
        {
            throw new class_6182(method_29275(sender));
        }
        else
        {
            if (!CarpetSettings.commandCameramode)
            {
                method_28710(sender, this, "Quick gamemode switching is disabled");
            }
            ServerPlayerEntity entityplayer = method_28708(sender);
            if(entityplayer.isSpectator()) return;
            if(CarpetSettings.cameraModeSurvivalRestrictions && entityplayer.interactionManager.getGameMode() == GameMode.SURVIVAL) {
                List<HostileEntity> hostiles = sender.getEntityWorld().getEntities(HostileEntity.class, new Box(entityplayer.x - 8.0D, entityplayer.y - 5.0D, entityplayer.z - 8.0D, entityplayer.x + 8.0D, entityplayer.y + 5.0D, entityplayer.z + 8.0D), mob -> mob.isAngryAt(entityplayer));
                StatusEffectInstance fireresist = entityplayer.getStatusEffect(StatusEffect.method_34297("fire_resistance"));
                if(!entityplayer.onGround || entityplayer.isFallFlying() || (((EntityAccessor) entityplayer).getFireTicks() > 0 && (fireresist == null || fireresist.getDuration() < ((EntityAccessor) entityplayer).getFireTicks())) || entityplayer.getAir() != 300 || !hostiles.isEmpty()){
                    method_28710(sender, this, "Restricted use to: on ground, not in water, not on fire, not flying/falling, not near hostile mobs.");
                    return;
                }
            }
            StatusEffect nightvision = StatusEffect.method_34297("night_vision");
            boolean hasNightvision = entityplayer.getStatusEffect(nightvision) != null;
            ((CameraPlayer) entityplayer).storeCameraData(hasNightvision);
            GameMode gametype = GameMode.byName("spectator", GameMode.NOT_SET);
            entityplayer.setGameMode(gametype);
            if(!hasNightvision) {
                StatusEffectInstance potioneffect = new StatusEffectInstance(nightvision, 999999, 0, false, false);
                entityplayer.addStatusEffect(potioneffect);
            }
            ((CameraPlayer) entityplayer).setGamemodeCamera();
        }
    }

    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.emptyList();
    }

}
