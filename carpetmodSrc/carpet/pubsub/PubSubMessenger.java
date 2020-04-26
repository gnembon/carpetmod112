package carpet.pubsub;

import carpet.network.PacketSplitter;
import carpet.network.PluginChannelHandler;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

import java.io.IOException;
import java.util.*;

public class PubSubMessenger implements PluginChannelHandler {
    public static final String CHANNEL_NAME = "carpet:pubsub";
    // reserve id 0 for now
    public static final int PACKET_C2S_SUBSCRIBE = 1;
    public static final int PACKET_C2S_UNSUBSCRIBE = 2;

    public static final int PACKET_S2C_UPDATE = 1;

    public static final int TYPE_NBT = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_FLOAT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_DOUBLE = 5;

    private final PubSubManager pubSub;
    private final Map<EntityPlayerMP, Map<PubSubNode, PubSubSubscriber>> subscriptions = new WeakHashMap<>();

    public PubSubMessenger(PubSubManager pubSub) {
        this.pubSub = pubSub;
    }

    @Override
    public String[] getChannels() {
        return new String[]{CHANNEL_NAME};
    }

    public void subscribe(EntityPlayerMP player, Collection<String> nodes) {
        Map<PubSubNode, PubSubSubscriber> playerSubscriptions = subscriptions.computeIfAbsent(player, p -> new HashMap<>());
        Set<PubSubNode> addedNodes = new HashSet<>();
        for (String nodeName : nodes) {
            PubSubNode node = pubSub.getNode(nodeName);
            if (node == null) continue;
            addedNodes.add(node);
        }
        Set<PubSubNode> deduplicatedNodes = new HashSet<>();
        for (PubSubNode node : addedNodes) {
            boolean alreadyAdded = false;
            for (PubSubNode n = node.parent; n != null; n = n.parent) {
                if (playerSubscriptions.containsKey(n) || addedNodes.contains(n)) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) deduplicatedNodes.add(node);
        }
        if (deduplicatedNodes.isEmpty()) return;
        PubSubSubscriber subscriber = (node, value) -> {
            PacketBuffer packet = makeUpdatePacket(Collections.singletonMap(node, value));
            PacketSplitter.send(player, CHANNEL_NAME, packet);
        };
        for (PubSubNode node : deduplicatedNodes) {
            playerSubscriptions.put(node, subscriber);
            pubSub.subscribe(node, subscriber);
        }
    }

    private void unsubscribe(EntityPlayerMP player, Collection<String> nodes) {
        Map<PubSubNode, PubSubSubscriber> playerSubscriptions = subscriptions.get(player);
        if (playerSubscriptions == null) return;
        for (String nodeName : nodes) {
            PubSubNode node = pubSub.getNode(nodeName);
            if (node == null) continue;
            PubSubSubscriber subscriber = playerSubscriptions.get(node);
            if (subscriber == null) continue;
            pubSub.unsubscribe(node, subscriber);
        }
    }

    /*
        Packet format (framed by PacketSplitter):
        id : varint = PACKET_S2C_UPDATE
        size : varint {
            node : string
            type : varint
            value : type dependent
        }[size]
     */
    private static PacketBuffer makeUpdatePacket(Map<PubSubNode, Object> updates) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(PACKET_S2C_UPDATE);
        buf.writeVarInt(updates.size());
        for (Map.Entry<PubSubNode, Object> update : updates.entrySet()) {
            String nodeName = update.getKey().fullName;
            Object value = update.getValue();
            buf.writeString(nodeName);
            if (value instanceof NBTBase) {
                NBTTagCompound tag = makeCompound((NBTBase) value);
                buf.writeVarInt(TYPE_NBT);
                ByteBufOutputStream out = new ByteBufOutputStream(buf);
                try {
                    CompressedStreamTools.write(tag, out);
                } catch (IOException ignored) {} // ByteBufOutputStream doesn't throw IOExceptions
            } else if (value instanceof String) {
                buf.writeVarInt(TYPE_STRING);
                buf.writeString((String) value);
            } else if (value instanceof Integer) {
                buf.writeVarInt(TYPE_INT);
                buf.writeInt((Integer) value);
            } else if (value instanceof Float) {
                buf.writeVarInt(TYPE_FLOAT);
                buf.writeFloat((Float) value);
            } else if (value instanceof Long) {
                buf.writeVarInt(TYPE_LONG);
                buf.writeLong((Long) value);
            } else if (value instanceof Double) {
                buf.writeVarInt(TYPE_DOUBLE);
                buf.writeDouble((Double) value);
            } else {
                throw new IllegalArgumentException("Can't serialize " + update.getClass().getSimpleName());
            }
        }
        return buf;
    }

    private static NBTTagCompound makeCompound(NBTBase tag) {
        if (tag instanceof NBTTagCompound) return (NBTTagCompound) tag;
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("", tag);
        return compound;
    }

    private static List<String> readNames(PacketBuffer buf) {
        int count = buf.readVarInt();
        List<String> names = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            names.add(buf.readString(32767));
        }
        return names;
    }

    @Override
    public void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player) {
        PacketBuffer payload = PacketSplitter.receive(player, packet);
        if (payload == null) return;
        int id = payload.readVarInt();
        switch (id) {
            case PACKET_C2S_SUBSCRIBE: {
                subscribe(player, readNames(payload));
                return;
            }
            case PACKET_C2S_UNSUBSCRIBE: {
                unsubscribe(player, readNames(payload));
                return;
            }
        }
        throw new IllegalArgumentException("Unknown packet id " + id + " for channel " + CHANNEL_NAME);
    }

    @Override
    public void unregister(String channel, EntityPlayerMP player) {
        if (!subscriptions.containsKey(player)) return;
        for (Map.Entry<PubSubNode, PubSubSubscriber> subscription : subscriptions.get(player).entrySet()) {
            subscription.getKey().unsubscribe(subscription.getValue());
        }
        subscriptions.remove(player);
    }
}
