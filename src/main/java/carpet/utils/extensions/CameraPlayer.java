package carpet.utils.extensions;

public interface CameraPlayer {
    void storeCameraData(boolean hasNightvision);
    void setGamemodeCamera();
    boolean getGamemodeCamera();
    boolean hadNightvision();
    boolean moveToStoredCameraData();
    boolean isDisableSpectatePlayers();
}
