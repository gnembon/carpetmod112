package carpet.patches;

 import net.minecraft.network.play.server.SPacketEntityHeadLook;
 import net.minecraft.network.play.server.SPacketEntityTeleport;
 import net.minecraft.network.play.server.SPacketPlayerListItem;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.management.PlayerInteractionManager;
 import com.mojang.authlib.GameProfile;
 import net.minecraft.tileentity.TileEntitySkull;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.text.TextComponentTranslation;
 import net.minecraft.world.WorldServer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.network.EnumPacketDirection;
  
 import net.minecraft.world.GameType;

public class EntityPlayerMPFake extends EntityPlayerMP
{
    private double oldPosX;
    private double oldPosY;
    private double oldPosZ;

    private static double setX;
    private static double setY;
    private static double setZ;
    private static float setYaw;
    private static float setPitch;
    
    public static EntityPlayerMPFake createFake(String username, MinecraftServer server, double x, double y, double z, double yaw, double pitch, int dimension, int gamemode)
    {
        setX = x;
        setY = y;
        setZ = z;
        setYaw = (float)yaw;
        setPitch = (float)pitch;
        
        WorldServer worldIn = server.getWorld(dimension);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getPlayerProfileCache().getGameProfileForUsername(username);
        gameprofile = profileCasher(gameprofile);
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), instance);
        if (instance.dimension != dimension) //player was logged in in a different dimension
        {
            WorldServer old_world = server.getWorld(instance.dimension);
            instance.dimension = dimension;
            old_world.removeEntityDangerously(instance);
            instance.isDead = false;
            worldIn.spawnEntity(instance);
            instance.setWorld(worldIn);
            server.getPlayerList().preparePlayer(instance, worldIn);
            instance.connection.setPlayerLocation(x, y, z, (float)yaw, (float)pitch);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.setHealth(20.0F);
        instance.isDead = false;
        instance.stepHeight = 0.6F;
        interactionManagerIn.setGameType(GameType.getByID(gamemode));
        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityHeadLook(instance, (byte)(instance.rotationYawHead * 256 / 360) ),instance.dimension);
        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityTeleport(instance),instance.dimension);
        server.getPlayerList().serverUpdateMovingPlayer(instance);
        return instance;
    }
    public static EntityPlayerMPFake createShadow(MinecraftServer server, EntityPlayerMP player)
    {
        player.getServer().getPlayerList().playerLoggedOut(player);
        player.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
        WorldServer worldIn = server.getWorld(player.dimension);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = player.getGameProfile();
        EntityPlayerMPFake playerShadow = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), playerShadow);

        playerShadow.setHealth(player.getHealth());
        playerShadow.connection.setPlayerLocation(player.posX, player.posY,player.posZ, player.rotationYaw, player.rotationPitch);
        interactionManagerIn.setGameType(player.interactionManager.getGameType());
        playerShadow.actionPack.copyFrom(player.actionPack);
        playerShadow.stepHeight = 0.6F;

        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityHeadLook(playerShadow, (byte)(player.rotationYawHead * 256 / 360) ),playerShadow.dimension);
        server.getPlayerList().sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, playerShadow));
        server.getPlayerList().serverUpdateMovingPlayer(playerShadow);
        return playerShadow;
    }

    private static GameProfile profileCasher(GameProfile gameprofile){
        if(!profileWithTexture(gameprofile) ) {
            gameprofile = TileEntitySkull.updateGameprofile(gameprofile);
        }
        return gameprofile;
    }

    private static boolean profileWithTexture(GameProfile gameprofile) {
        return gameprofile.getProperties().containsKey("textures");
    }

    private EntityPlayerMPFake(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn)
    {
        super(server, worldIn, profile, interactionManagerIn);
    }

    public void onKillCommand()
    {
        //super.onKillCommand();
        this.getServer().getPlayerList().playerLoggedOut(this);
    }

    public void onUpdate()
    {
        super.onUpdate();
        this.onUpdateEntity();
        playerMoved();
    }

    private void playerMoved() {
        if(posX != oldPosX || posY != oldPosY || posZ != oldPosZ){
            connection.server.getPlayerList().serverUpdateMovingPlayer(this);
        }
        oldPosX = posX;
        oldPosY = posY;
        oldPosZ = posZ;
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        getServer().getPlayerList().playerLoggedOut(this);
    }

    public void postReadFromNBT(){
        this.setLocationAndAngles(setX, setY, setZ, setYaw, setPitch);
    }
}
