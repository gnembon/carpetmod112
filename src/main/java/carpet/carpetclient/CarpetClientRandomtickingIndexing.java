package carpet.carpetclient;

import carpet.CarpetSettings;
import net.minecraft.class_6380;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CarpetClientRandomtickingIndexing {

    private static boolean[] updates = {false, false, false};
    private static boolean enableUpdates = false;
    private static List<ServerPlayerEntity> players = new ArrayList<>();

    public static void enableUpdate(ServerPlayerEntity player) {
        if (!enableUpdates) return;
        int dimention = player.world.dimension.getType().getRawId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static boolean sendUpdates(World world) {
        int dimention = world.dimension.getType().getRawId() + 1;
        return updates[dimention];
    }

    public static void register(ServerPlayerEntity sender, PacketByteBuf data) {
        boolean register = data.readBoolean();
        if (register) {
            registerPlayer(sender);
        } else {
            unregisterPlayer(sender);
        }
    }

    private static void registerPlayer(ServerPlayerEntity sender) {
        players.add(sender);
        enableUpdates = true;
        int dimention = sender.world.dimension.getType().getRawId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static void unregisterPlayer(ServerPlayerEntity player) {
        players.remove(player);
        if (players.size() == 0) enableUpdates = false;
    }

    public static void sendRandomtickingChunkOrder(World world, class_6380 playerChunkMap) {
        CompoundTag compound = new CompoundTag();
        ListTag nbttaglist = new ListTag();
        for (Iterator<WorldChunk> iterator = playerChunkMap.method_33585(); iterator.hasNext(); ) {
            WorldChunk c = iterator.next();
            CompoundTag chunkData = new CompoundTag();
            chunkData.putInt("x", c.field_25365);
            chunkData.putInt("z", c.field_25366);
            nbttaglist.add(chunkData);
        }
        compound.put("list", nbttaglist);
        for (ServerPlayerEntity p : players) {
            CarpetClientMessageHandler.sendNBTRandomTickData(p, compound);
        }

        int dimention = world.dimension.getType().getRawId() + 1;
        updates[dimention] = false;
    }

}
