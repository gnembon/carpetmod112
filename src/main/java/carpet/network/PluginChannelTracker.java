package carpet.network;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginChannelTracker {
    private final MinecraftServer server;

    // A multimap from player names to the channels they are registered on
    private SetMultimap<String, String> name2channels = MultimapBuilder.hashKeys().hashSetValues().build();

    // A multimap from channel names to the names of players registered to that channel
    private SetMultimap<String, String> channel2names = MultimapBuilder.hashKeys().hashSetValues().build();

    public PluginChannelTracker(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Returns the collection of channels {@code player} is registered to.
     */
    public Set<String> getChannels(ServerPlayerEntity player) {
        return name2channels.get(player.getName());
    }

    /**
     * Returns whether or not {@code player} is reigstered to {@code channel}.
     */
    public boolean isRegistered(ServerPlayerEntity player, String channel) {
        return name2channels.containsEntry(player.getName(), channel);
    }

    /**
     * Returns the collection of names of players registered to {@code channel}.
     */
    public Set<String> getPlayerNames(String channel) {
        return channel2names.get(channel);
    }

    /**
     * Returns the collection of players registered to {@code channel}. The {@code server} is used to look players up
     * by their name.
     */
    public Set<ServerPlayerEntity> getPlayers(String channel) {
        PlayerManager pl = server.getPlayerManager();
        return channel2names.get(channel).stream()
                .map(pl::getPlayer)
                .collect(Collectors.toSet());
    }

    /**
     * Registers {@code player} on {@code channel}.
     */
    public void register(ServerPlayerEntity player, String channel) {
        name2channels.put(player.getName(), channel);
        channel2names.put(channel, player.getName());
    }

    /**
     * Unregisters {@code player} from {@code channel}.
     */
    public void unregister(ServerPlayerEntity player, String channel) {
        name2channels.remove(player.getName(), channel);
        channel2names.remove(channel, player.getName());
    }

    /**
     * Unregisters {@code player} from all channels.
     */
    public void unregisterAll(ServerPlayerEntity player) {
        for (String channel : getChannels(player)) {
            channel2names.remove(channel, player.getName());
        }
        name2channels.removeAll(player.getName());
    }

}
