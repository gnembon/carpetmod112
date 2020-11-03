package carpet.helpers;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;

public class CameraData {
    public double storeX;
    public double storeY;
    public double storeZ;
    public float storeYaw;
    public float storePitch;
    public int storedDim;
    public boolean disableSpectatePlayers;
    public boolean gamemodeCamera;
    public boolean nightvision;

    public CameraData() {}

    public CameraData(EntityPlayerMP player, boolean hasNightvision, boolean gamemodeCamera) {
        storeX = player.posX;
        storeY = player.posY;
        storeZ = player.posZ;
        storeYaw = player.rotationYaw;
        storePitch = player.rotationPitch;
        storedDim = player.dimension;
        disableSpectatePlayers = CarpetSettings.cameraModeDisableSpectatePlayers;
        nightvision = hasNightvision;
        this.gamemodeCamera = gamemodeCamera;
    }
}
