package carpet.patches;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;

public class NetworkManagerFake extends ClientConnection {
    public NetworkManagerFake(NetworkSide p) {
        super(p);
    }

    @Override
    public void disableAutoRead() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void handleDisconnection() {
    }
}
