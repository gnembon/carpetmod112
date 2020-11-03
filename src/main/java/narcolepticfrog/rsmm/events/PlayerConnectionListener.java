package narcolepticfrog.rsmm.events;

import net.minecraft.entity.player.EntityPlayerMP;

public interface PlayerConnectionListener {

    public void onPlayerConnect(EntityPlayerMP player);

    public void onPlayerDisconnect(EntityPlayerMP player);

}
