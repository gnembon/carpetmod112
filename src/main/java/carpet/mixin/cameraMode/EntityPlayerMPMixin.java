package carpet.mixin.cameraMode;

import carpet.CarpetSettings;
import carpet.helpers.CameraData;
import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.extensions.CameraPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer implements CameraPlayer {
    private CameraData cameraData = new CameraData();

    public @Final @Shadow MinecraftServer server;
    public @Shadow NetHandlerPlayServer connection;
    public @Final @Shadow PlayerInteractionManager interactionManager;
    public abstract @Shadow WorldServer getServerWorld();

    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void onCopyFrom(EntityPlayerMP that, boolean keepEverything, CallbackInfo ci) {
        cameraData = ((EntityPlayerMPMixin) (Object) that).cameraData;
    }

    @Inject(method = "setGameType", at = @At("RETURN"))
    private void onGameModeChange(GameType gameType, CallbackInfo ci) {
        if (gameType != GameType.SPECTATOR) {
            // Rule to prevent /c camera mode to spectate other players, disable after exiting spectator mode CARPET-XCOM
            cameraData.disableSpectatePlayers = false;
        }
        cameraData.gamemodeCamera = false;
    }

    @Override
    public void storeCameraData(boolean hasNightvision) {
        cameraData = new CameraData(asPlayer(), hasNightvision, cameraData.gamemodeCamera);
    }

    @Override
    public void setGamemodeCamera() {
        cameraData.gamemodeCamera = true;
    }

    @Override
    public boolean getGamemodeCamera() {
        return cameraData.gamemodeCamera;
    }

    @Override
    public boolean hadNightvision() {
        return cameraData.nightvision;
    }

    @Override
    public boolean isDisableSpectatePlayers() {
        return cameraData.disableSpectatePlayers;
    }

    private EntityPlayerMP asPlayer() {
        return (EntityPlayerMP) (Object) this;
    }

    @Override
    public boolean moveToStoredCameraData() {
        if (CarpetSettings.cameraModeRestoreLocation) {
            if (cameraData.storedDim != dimension) {
                WorldServer worldserver1 = getServerWorld();
                WorldServer worldserver2 = server.getWorld(cameraData.storedDim);
                dimension = cameraData.storedDim;
                connection.sendPacket(new SPacketRespawn(dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), this.interactionManager.getGameType()));
                this.server.getPlayerList().updatePermissionLevel(asPlayer());
                DebugLogHelper.invisDebug(() -> "s2: " + worldserver1.loadedEntityList.contains(this) + " " + worldserver2.loadedEntityList.contains(this));
                worldserver1.removeEntity(this);
                isDead = false;
                worldserver1.getChunk(chunkCoordX, chunkCoordZ).removeEntityAtIndex(this, chunkCoordY);

                if (isEntityAlive()) {
                    setLocationAndAngles(cameraData.storeX, cameraData.storeY, cameraData.storeZ, cameraData.storeYaw, cameraData.storePitch);
                    worldserver2.spawnEntity(this);
                    worldserver2.updateEntityWithOptionalForce(this, false);
                }
                DebugLogHelper.invisDebug(() -> "s3: " + worldserver1.loadedEntityList.contains(this) + " " + worldserver2.loadedEntityList.contains(this));
                setWorld(worldserver2);
                this.server.getPlayerList().preparePlayer(asPlayer(), worldserver1);
                setPositionAndUpdate(cameraData.storeX, cameraData.storeY, cameraData.storeZ);
                interactionManager.setWorld(worldserver2);
                this.server.getPlayerList().updateTimeAndWeatherForPlayer(asPlayer(), worldserver2);
                this.server.getPlayerList().syncPlayerInventory(asPlayer());
                DebugLogHelper.invisDebug(() -> "s4: " + worldserver1.loadedEntityList.contains(this) + " " + worldserver2.loadedEntityList.contains(this));
                return true;
            } else {
                if (cameraData.storeX == 0 && cameraData.storeY == 0 && cameraData.storeZ == 0)
                    cameraData.storeY = 256.0f;
                double dist = Math.sqrt(new BlockPos(cameraData.storeX, cameraData.storeY, cameraData.storeZ).distanceSq(posX, posY, posZ));
                connection.setPlayerLocation(cameraData.storeX, cameraData.storeY, cameraData.storeZ, cameraData.storeYaw, cameraData.storePitch);
                return dist > (this.server.getPlayerList().getViewDistance() - 2) * 16;
            }
        }
        return false;
    }
}
