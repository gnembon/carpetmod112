package carpet.helpers;

import carpet.CarpetSettings;
import net.minecraft.server.network.ServerPlayerEntity;

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

    public CameraData(ServerPlayerEntity player, boolean hasNightvision, boolean gamemodeCamera) {
        storeX = player.field_33071;
        storeY = player.field_33072;
        storeZ = player.field_33073;
        storeYaw = player.yaw;
        storePitch = player.pitch;
        storedDim = player.field_33045;
        disableSpectatePlayers = CarpetSettings.cameraModeDisableSpectatePlayers;
        nightvision = hasNightvision;
        this.gamemodeCamera = gamemodeCamera;
    }
}
