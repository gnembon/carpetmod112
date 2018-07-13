package carpet.patches;

 import net.minecraft.network.NetworkManager;
  import net.minecraft.network.EnumPacketDirection;
  
 //import net.minecraft.world.GameType;
 //import org.apache.logging.log4j.LogManager;
 //import org.apache.logging.log4j.Logger;

public class NetworkManagerFake extends NetworkManager
{
	//private static final Logger LOGGER = LogManager.getLogger();
	public NetworkManagerFake(EnumPacketDirection p)
	{
		super(p);
	}
	
	public void disableAutoRead()
	{
		;
	}
	public void checkDisconnected()
	{
		;
	}
}
