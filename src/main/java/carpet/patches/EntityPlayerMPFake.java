package carpet.patches;

import carpet.CarpetSettings;
import carpet.utils.extensions.ActionPackOwner;
import carpet.utils.extensions.CameraPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public class EntityPlayerMPFake extends EntityPlayerMP
{
    private static final ThreadLocal<Boolean> loginMinecartFix = new ThreadLocal<>();
    private double lastReportedPosX;
    private double lastReportedPosY;
    private double lastReportedPosZ;

    private double setX;
    private double setY;
    private double setZ;
    private float setYaw;
    private float setPitch;

    public static boolean shouldFixMinecart() {
        Boolean fix = loginMinecartFix.get();
        return fix != null && fix;
    }

    private static void setShouldFixMinecart(boolean fix) {
        loginMinecartFix.set(fix);
    }

    public static EntityPlayerMPFake createFake(String username, MinecraftServer server, double x, double y, double z, double yaw, double pitch, int dimension, int gamemode)
    {
        WorldServer worldIn = server.getWorld(dimension);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getPlayerProfileCache().getGameProfileForUsername(username);
        if (gameprofile == null) {
            UUID uuid = EntityPlayer.getUUID(new GameProfile((UUID)null, username));
            gameprofile = new GameProfile(uuid, username);
        }else {
            gameprofile = fixSkin(gameprofile);
        }
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        instance.setSetPosition(x, y, z, (float)yaw, (float)pitch);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), instance);
        if (instance.dimension != dimension) //player was logged in in a different dimension
        {
            WorldServer old_world = server.getWorld(instance.dimension);
            instance.dimension = dimension;
            old_world.removeEntity(instance);
            instance.isDead = false;
            worldIn.spawnEntity(instance);
            instance.setWorld(worldIn);
            server.getPlayerList().preparePlayer(instance, old_world);
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
        instance.dataManager.set(PLAYER_MODEL_FLAG, (byte) 0x7f); // show all model layers (incl. capes)
        createAndAddFakePlayerToTeamBot(instance);
        return instance;
    }

    public static EntityPlayerMPFake createShadow(MinecraftServer server, EntityPlayerMP player)
    {
        if(CarpetSettings.cameraModeRestoreLocation && ((CameraPlayer) player).getGamemodeCamera()) {
            GameType gametype = server.getGameType();
            ((CameraPlayer) player).moveToStoredCameraData();
            player.setGameType(gametype);
            player.removePotionEffect(Potion.getPotionFromResourceLocation("night_vision"));
        }
        player.getServer().getPlayerList().playerLoggedOut(player);
        player.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
        WorldServer worldIn = server.getWorld(player.dimension);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = player.getGameProfile();
        gameprofile = fixSkin(gameprofile);
        EntityPlayerMPFake playerShadow = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        playerShadow.setSetPosition(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), playerShadow);

        playerShadow.setHealth(player.getHealth());
        playerShadow.connection.setPlayerLocation(player.posX, player.posY,player.posZ, player.rotationYaw, player.rotationPitch);
        interactionManagerIn.setGameType(player.interactionManager.getGameType());
        ((ActionPackOwner) playerShadow).getActionPack().copyFrom(((ActionPackOwner) player).getActionPack());
        playerShadow.stepHeight = 0.6F;

        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityHeadLook(playerShadow, (byte)(player.rotationYawHead * 256 / 360) ),playerShadow.dimension);
        server.getPlayerList().sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, playerShadow));
        server.getPlayerList().serverUpdateMovingPlayer(playerShadow);
        createAndAddFakePlayerToTeamBot(playerShadow);
        return playerShadow;
    }

    public static EntityPlayerMPFake create(String info, MinecraftServer server)
    {
        String[] infos = info.split("/");
        String username = infos[0];
        WorldServer worldIn = server.getWorld(0);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getPlayerProfileCache().getGameProfileForUsername(username);
        if (gameprofile == null) {
            UUID uuid = EntityPlayer.getUUID(new GameProfile((UUID)null, username));
            gameprofile = new GameProfile(uuid, username);
        } else {
            gameprofile = fixSkin(gameprofile);
        }
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        server.getPlayerList().readPlayerDataFromFile(instance);
        instance.setSetPosition(instance.posX, instance.posY, instance.posZ, instance.rotationYaw, instance.rotationPitch);
        setShouldFixMinecart(true);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), instance);
        setShouldFixMinecart(false);
        if (instance.dimension != 0) //player was logged in in a different dimension
        {
            worldIn = server.getWorld(instance.dimension);
            instance.setWorld(worldIn);
            server.getPlayerList().preparePlayer(instance, worldIn);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.isDead = false;
        instance.stepHeight = 0.6F;
        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityHeadLook(instance, (byte)(instance.rotationYawHead * 256 / 360) ),instance.dimension);
        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityTeleport(instance),instance.dimension);
        server.getPlayerList().serverUpdateMovingPlayer(instance);
        instance.dataManager.set(PLAYER_MODEL_FLAG, (byte) 0x7f); // show all model layers (incl. capes)
        createAndAddFakePlayerToTeamBot(instance);
        if(infos.length > 1) ((ActionPackOwner) instance).getActionPack().fromString(infos[1]);
        return instance;
    }

    private EntityPlayerMPFake(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn)
    {
        super(server, worldIn, profile, interactionManagerIn);
    }

    private static GameProfile fixSkin(GameProfile gameProfile)
    {
        if (!CarpetSettings.removeFakePlayerSkins && !gameProfile.getProperties().containsKey("texture"))
            return TileEntitySkull.updateGameProfile(gameProfile);
        else
            return gameProfile;
    }

    @Override
    public void onKillCommand()
    {
        logout();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        this.onUpdateEntity();
        this.playerMoved();
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        logout();
    }

    private void logout() {
        this.dismountRidingEntity();
        connection.onDisconnect(new TextComponentString("Logout"));
        removePlayerFromTeams(this);
    }

    public void despawn() {
        connection.onDisconnect(new TextComponentString("Despawn"));
        removePlayerFromTeams(this);
    }

    private void playerMoved()
    {
        if (posX != lastReportedPosX || posY != lastReportedPosY || posZ != lastReportedPosZ)
        {
            server.getPlayerList().serverUpdateMovingPlayer(this);
            lastReportedPosX = posX;
            lastReportedPosY = posY;
            lastReportedPosZ = posZ;
        }
    }

    public void setSetPosition(double x, double y, double z, float yaw, float pitch)
    {
        this.setX = x;
        this.setY = y;
        this.setZ = z;
        this.setYaw = yaw;
        this.setPitch = pitch;
    }

    public void resetToSetPosition()
    {
        setLocationAndAngles(setX, setY, setZ, setYaw, setPitch);
    }

    private static void createAndAddFakePlayerToTeamBot(EntityPlayerMPFake player)
    {
        Scoreboard scoreboard = player.getServer().getWorld(0).getScoreboard();
        if(!scoreboard.getTeamNames().contains("Bots")){
            scoreboard.createTeam("Bots");
            ScorePlayerTeam scoreplayerteam = scoreboard.getTeam("Bots");
            TextFormatting textformatting = TextFormatting.getValueByName("dark_green");
            scoreplayerteam.setColor(textformatting);
            scoreplayerteam.setPrefix(textformatting.toString());
            scoreplayerteam.setSuffix(TextFormatting.RESET.toString());
        }
        scoreboard.addPlayerToTeam(player.getName(), "Bots");
    }

    public static void removePlayerFromTeams(EntityPlayerMPFake player){
        Scoreboard scoreboard = player.getServer().getWorld(0).getScoreboard();
        scoreboard.removePlayerFromTeams(player.getName());
    }

    public static String getInfo(EntityPlayerMP p){
        return p.getName() + "/" + ((ActionPackOwner) p).getActionPack();
    }

    @Override
    public void addStat(StatBase stat, int amount) {
        if (CarpetSettings.fakePlayerStats) {
            super.addStat(stat, amount);
        }
    }
}
