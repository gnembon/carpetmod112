package carpet.utils;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;

import java.util.Set;
import java.util.stream.Collectors;

public class PluginChannelTracker {

    // A multimap from player names to the channels they are registered on
    private static SetMultimap<String, String> name2channels = MultimapBuilder.hashKeys().hashSetValues().build();

    // A multimap from channel names to the names of players registered to that channel
    private static SetMultimap<String, String> channel2names = MultimapBuilder.hashKeys().hashSetValues().build();

    /**
     * Returns the collection of channels {@code player} is registered to.
     */
    public static Set<String> getChannels(EntityPlayerMP player) {
        return name2channels.get(player.getName());
    }

    /**
     * Returns whether or not {@code player} is reigstered to {@code channel}.
     */
    public static boolean isRegistered(EntityPlayerMP player, String channel) {
        return name2channels.containsEntry(player.getName(), channel);
    }

    /**
     * Returns the collection of names of players registered to {@code channel}.
     */
    public static Set<String> getPlayerNames(String channel) {
        return channel2names.get(channel);
    }

    /**
     * Returns the collection of players registered to {@code channel}. The {@code server} is used to look players up
     * by their name.
     */
    public static Set<EntityPlayerMP> getPlayers(MinecraftServer server, String channel) {
        PlayerList pl = server.getPlayerList();
        return channel2names.get(channel).stream()
                .map(pl::getPlayerByUsername)
                .collect(Collectors.toSet());
    }

    /**
     * Registers {@code player} on {@code channel}.
     */
    public static void register(EntityPlayerMP player, String channel) {
        name2channels.put(player.getName(), channel);
        channel2names.put(channel, player.getName());
    }

    /**
     * Unregisters {@code player} from {@code channel}.
     */
    public static void unregister(EntityPlayerMP player, String channel) {
        name2channels.remove(player.getName(), channel);
        channel2names.remove(channel, player.getName());
    }

    /**
     * Unregisters {@code player} from all channels.
     */
    public static void unregisterAll(EntityPlayerMP player) {
        for (String channel : getChannels(player)) {
            channel2names.remove(channel, player.getName());
        }
        name2channels.removeAll(player.getName());
    }

}
