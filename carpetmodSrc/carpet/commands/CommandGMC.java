package carpet.commands;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

import net.minecraft.command.WrongUsageException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class CommandGMC extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "c";
    }

    /**
     * Gets the usage string for the command.
     *  
     * @param sender The ICommandSender who is requesting usage details
     */
    public String getUsage(ICommandSender sender)
    {
        return "commands.gamemode.usage";
    }

    /**
     * Callback for when the command is executed
     *  
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args The arguments that were passed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, NumberInvalidException
    {
        if (!command_enabled("commandCameramode", sender)) return;
        if (args.length > 0)
        {
            throw new WrongUsageException(getUsage(sender), new Object[0]);
        }
        else
        {
            if (!CarpetSettings.commandCameramode)
            {
                notifyCommandListener(sender, this, "Quick gamemode switching is disabled");
            }
            EntityPlayerMP entityplayer = getCommandSenderAsPlayer(sender);
            if(entityplayer.isSpectator()) return;
            if(CarpetSettings.cameraModeSurvivalRestrictions && entityplayer.isSurvival()) {
                List<EntityMob> hostiles = sender.getEntityWorld().getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(entityplayer.posX - 8.0D, entityplayer.posY - 5.0D, entityplayer.posZ - 8.0D, entityplayer.posX + 8.0D, entityplayer.posY + 5.0D, entityplayer.posZ + 8.0D), new EntityPlayer.SleepEnemyPredicate(entityplayer));
                PotionEffect fireresist = entityplayer.getActivePotionEffect(Potion.getPotionFromResourceLocation("fire_resistance"));
                if(!entityplayer.onGround || entityplayer.isElytraFlying() || (entityplayer.getFire() > 0 && (fireresist == null || fireresist.getDuration() < entityplayer.getFire())) || entityplayer.getAir() != 300 || !hostiles.isEmpty()){
                    notifyCommandListener(sender, this, "Restricted use to: on ground, not in water, not on fire, not flying/falling, not near hostile mobs.");
                    return;
                }
            }
            Potion nightvision = Potion.getPotionFromResourceLocation("night_vision");
            boolean hasNightvision = entityplayer.getActivePotionEffect(nightvision) != null;
            entityplayer.storeCameraData(hasNightvision);
            GameType gametype = GameType.parseGameTypeWithDefault("spectator", GameType.NOT_SET);
            entityplayer.setGameType(gametype);
            if(!hasNightvision) {
                PotionEffect potioneffect = new PotionEffect(nightvision, 999999, 0, false, false);
                entityplayer.addPotionEffect(potioneffect);
            }
            entityplayer.setGamemodeCamera();
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.<String>emptyList();
    }

}
