package carpet.patches;

import carpet.CarpetSettings;
import carpet.utils.extensions.ActionPackOwner;
import carpet.utils.extensions.CameraPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import java.util.UUID;

public class FakeServerPlayerEntity extends ServerPlayerEntity
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

    public static FakeServerPlayerEntity createFake(String username, MinecraftServer server, double x, double y, double z, double yaw, double pitch, int dimension, int gamemode)
    {
        ServerWorld worldIn = server.getWorldById(dimension);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getUserCache().findByName(username);
        if (gameprofile == null) {
            UUID uuid = PlayerEntity.getUuidFromProfile(new GameProfile((UUID)null, username));
            gameprofile = new GameProfile(uuid, username);
        }else {
            gameprofile = fixSkin(gameprofile);
        }
        FakeServerPlayerEntity instance = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
        instance.setSetPosition(x, y, z, (float)yaw, (float)pitch);
        server.getPlayerManager().onPlayerConnect(new NetworkManagerFake(NetworkSide.CLIENTBOUND), instance);
        if (instance.dimensionId != dimension) //player was logged in in a different dimension
        {
            ServerWorld old_world = server.getWorldById(instance.dimensionId);
            instance.dimensionId = dimension;
            old_world.removeEntity(instance);
            instance.removed = false;
            worldIn.spawnEntity(instance);
            instance.setWorld(worldIn);
            server.getPlayerManager().method_33707(instance, old_world);
            instance.networkHandler.requestTeleport(x, y, z, (float)yaw, (float)pitch);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.setHealth(20.0F);
        instance.removed = false;
        instance.stepHeight = 0.6F;
        interactionManagerIn.setGameMode(GameMode.byId(gamemode));
        server.getPlayerManager().method_33699(new EntitySetHeadYawS2CPacket(instance, (byte)(instance.headYaw * 256 / 360) ),instance.dimensionId);
        server.getPlayerManager().method_33699(new EntityPositionS2CPacket(instance),instance.dimensionId);
        server.getPlayerManager().updateCameraPosition(instance);
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
        createAndAddFakePlayerToTeamBot(instance);
        return instance;
    }

    public static FakeServerPlayerEntity createShadow(MinecraftServer server, ServerPlayerEntity player)
    {
        if (CarpetSettings.cameraModeRestoreLocation && ((CameraPlayer) player).getGamemodeCamera()) {
            GameMode gametype = server.getDefaultGameMode();
            ((CameraPlayer) player).moveToStoredCameraData();
            player.setGameMode(gametype);
            player.removeStatusEffect(StatusEffect.method_34297("night_vision"));
        }
        player.getServer().getPlayerManager().remove(player);
        player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login"));
        ServerWorld worldIn = server.getWorldById(player.dimensionId);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = player.getGameProfile();
        gameprofile = fixSkin(gameprofile);
        FakeServerPlayerEntity playerShadow = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
        playerShadow.setSetPosition(player.x, player.y, player.z, player.yaw, player.pitch);
        server.getPlayerManager().onPlayerConnect(new NetworkManagerFake(NetworkSide.CLIENTBOUND), playerShadow);

        playerShadow.setHealth(player.getHealth());
        playerShadow.networkHandler.requestTeleport(player.x, player.y,player.z, player.yaw, player.pitch);
        interactionManagerIn.setGameMode(player.interactionManager.getGameMode());
        ((ActionPackOwner) playerShadow).getActionPack().copyFrom(((ActionPackOwner) player).getActionPack());
        playerShadow.stepHeight = 0.6F;

        server.getPlayerManager().method_33699(new EntitySetHeadYawS2CPacket(playerShadow, (byte)(player.headYaw * 256 / 360) ), playerShadow.dimensionId);
        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, playerShadow));
        server.getPlayerManager().updateCameraPosition(playerShadow);
        createAndAddFakePlayerToTeamBot(playerShadow);
        return playerShadow;
    }

    public static FakeServerPlayerEntity create(String info, MinecraftServer server)
    {
        String[] infos = info.split("/");
        String username = infos[0];
        ServerWorld worldIn = server.getWorldById(0);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getUserCache().findByName(username);
        if (gameprofile == null) {
            UUID uuid = PlayerEntity.getUuidFromProfile(new GameProfile(null, username));
            gameprofile = new GameProfile(uuid, username);
        } else {
            gameprofile = fixSkin(gameprofile);
        }
        FakeServerPlayerEntity instance = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
        server.getPlayerManager().loadPlayerData(instance);
        instance.setSetPosition(instance.x, instance.y, instance.z, instance.yaw, instance.pitch);
        setShouldFixMinecart(true);
        server.getPlayerManager().onPlayerConnect(new NetworkManagerFake(NetworkSide.CLIENTBOUND), instance);
        setShouldFixMinecart(false);
        if (instance.dimensionId != 0) //player was logged in in a different dimension
        {
            worldIn = server.getWorldById(instance.dimensionId);
            instance.setWorld(worldIn);
            server.getPlayerManager().method_33707(instance, worldIn);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.removed = false;
        instance.stepHeight = 0.6F;
        server.getPlayerManager().method_33699(new EntitySetHeadYawS2CPacket(instance, (byte)(instance.headYaw * 256 / 360) ),instance.dimensionId);
        server.getPlayerManager().method_33699(new EntityPositionS2CPacket(instance),instance.dimensionId);
        server.getPlayerManager().updateCameraPosition(instance);
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
        createAndAddFakePlayerToTeamBot(instance);
        if(infos.length > 1) ((ActionPackOwner) instance).getActionPack().fromString(infos[1]);
        return instance;
    }

    private FakeServerPlayerEntity(MinecraftServer server, ServerWorld worldIn, GameProfile profile, ServerPlayerInteractionManager interactionManagerIn)
    {
        super(server, worldIn, profile, interactionManagerIn);
    }

    private static GameProfile fixSkin(GameProfile gameProfile)
    {
        if (!CarpetSettings.removeFakePlayerSkins && !gameProfile.getProperties().containsKey("texture"))
            return SkullBlockEntity.loadProperties(gameProfile);
        else
            return gameProfile;
    }

    @Override
    public void kill()
    {
        logout();
    }

    @Override
    public void tick()
    {
        super.tick();
        this.playerTick();
        this.playerMoved();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        logout();
    }

    private void logout() {
        this.stopRiding();
        networkHandler.onDisconnected(new LiteralText("Logout"));
        removePlayerFromTeams(this);
    }

    public void despawn() {
        networkHandler.onDisconnected(new LiteralText("Despawn"));
        removePlayerFromTeams(this);
    }

    private void playerMoved()
    {
        if (x != lastReportedPosX || y != lastReportedPosY || z != lastReportedPosZ)
        {
            server.getPlayerManager().updateCameraPosition(this);
            lastReportedPosX = x;
            lastReportedPosY = y;
            lastReportedPosZ = z;
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
        refreshPositionAndAngles(setX, setY, setZ, setYaw, setPitch);
    }

    private static void createAndAddFakePlayerToTeamBot(FakeServerPlayerEntity player)
    {
        Scoreboard scoreboard = player.getServer().getWorldById(0).getScoreboard();
        if(!scoreboard.getTeamNames().contains("Bots")){
            scoreboard.addTeam("Bots");
            Team team = scoreboard.getTeam("Bots");
            Formatting textformatting = Formatting.byName("dark_green");
            team.method_28573(textformatting);
            team.method_28580(textformatting.toString());
            team.method_28583(Formatting.RESET.toString());
        }
        scoreboard.addPlayerToTeam(player.getName(), "Bots");
    }

    public static void removePlayerFromTeams(FakeServerPlayerEntity player){
        Scoreboard scoreboard = player.getServer().getWorldById(0).getScoreboard();
        scoreboard.clearPlayerTeam(player.getName());
    }

    public static String getInfo(ServerPlayerEntity p){
        return p.getName() + "/" + ((ActionPackOwner) p).getActionPack();
    }

    @Override
    public void increaseStat(Stat stat, int amount) {
        if (CarpetSettings.fakePlayerStats) {
            super.increaseStat(stat, amount);
        }
    }
}
