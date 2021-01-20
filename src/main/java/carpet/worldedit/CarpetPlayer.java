package carpet.worldedit;

import java.util.UUID;

import javax.annotation.Nullable;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;

import carpet.CarpetServer;
import io.netty.buffer.Unpooled;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;

@SuppressWarnings("deprecation")
class CarpetPlayer extends AbstractPlayerActor {

    private final ServerPlayerEntity player;

    protected CarpetPlayer(ServerPlayerEntity player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public int getItemInHand() {
        ItemStack is = this.player.getMainHandStack();
        return is == null ? 0 : Item.getRawId(is.getItem());
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        Vector position = new Vector(this.player.x, this.player.y, this.player.z);
        return new Location(
                CarpetWorldEdit.inst.getWorld(this.player.world),
                position,
                this.player.strideDistance,
                this.player.cameraPitch);
    }

    @Override
    public WorldVector getPosition() {
        return new WorldVector(LocalWorldAdapter.adapt(CarpetWorldEdit.inst.getWorld(this.player.world)), this.player.x, this.player.y, this.player.z);
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return CarpetWorldEdit.inst.getWorld(this.player.world);
    }

    @Override
    public double getPitch() {
        return this.player.pitch;
    }

    @Override
    public double getYaw() {
        return this.player.yaw;
    }

    @Override
    public void giveItem(int type, int amt) {
        this.player.inventory.insertStack(new ItemStack(Item.byRawId(type), amt, 0));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        PacketByteBuf pb = new PacketByteBuf(Unpooled.wrappedBuffer(send.getBytes(WECUIPacketHandler.UTF_8_CHARSET)));
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CarpetWorldEdit.CUI_PLUGIN_CHANNEL, pb);
        this.player.networkHandler.sendPacket(packet);
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendSystemMessage(new LiteralText(part));
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendSystemMessage(new LiteralText("\u00a77" + part));
        }
    }

    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendSystemMessage(new LiteralText("\u00a7d" + part));
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendSystemMessage(new LiteralText("\u00a7c" + part));
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        this.player.networkHandler.requestTeleport(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
    }

    @Override
    public String[] getGroups() {
        return new String[]{}; // WorldEditMod.inst.getPermissionsResolver().getGroups(this.player.username);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String perm) {
        int opLevel = CarpetServer.minecraft_server.getPlayerManager().getOpList().method_33753(player.getGameProfile());
        int requiredOpLevel = CarpetWorldEdit.inst.getConfig().getPermissionLevel(perm);
        return opLevel >= requiredOpLevel;
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player.getUuid(), player.getName());
    }

    private static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        private SessionKeyImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isActive() {
            // We can't directly check if the player is online because
            // the list of players is not thread safe
            return ThreadSafeCache.getInstance().getOnlineIds().contains(uuid);
        }

        @Override
        public boolean isPersistent() {
            return true;
        }

    }

}