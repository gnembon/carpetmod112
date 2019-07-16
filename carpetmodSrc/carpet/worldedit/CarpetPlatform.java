package carpet.worldedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.util.PropertiesConfiguration;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.world.World;

import carpet.CarpetServer;
import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

class CarpetPlatform extends AbstractPlatform implements MultiUserPlatform {

    private final CarpetWorldEdit mod;
    private final MinecraftServer server;
    private boolean hookingEvents = false;

    CarpetPlatform(CarpetWorldEdit mod) {
        this.mod = mod;
        this.server = CarpetServer.minecraft_server;
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public int resolveItem(String name) {
        if (name == null) return 0;

        int index = name.indexOf(':');

        if (index != -1 && index != 0 && index != name.length() - 1) {
            Block block = Block.getBlockFromName(name);
            if (block != null) {
                return Block.getIdFromBlock(block);
            }
        }

        for (Object itemObj : Item.REGISTRY) {
            Item item = (Item) itemObj;
            if (item == null) continue;
            if (item.getTranslationKey() == null) continue;
            if (item.getTranslationKey().startsWith("item.")) {
                if (item.getTranslationKey().equalsIgnoreCase("item." + name)) return Item.getIdFromItem(item);
            }
            if (item.getTranslationKey().startsWith("tile.")) {
                if (item.getTranslationKey().equalsIgnoreCase("tile." + name)) return Item.getIdFromItem(item);
            }
            if (item.getTranslationKey().equalsIgnoreCase(name)) return Item.getIdFromItem(item);
        }
        return 0;
    }

    @Override
    public boolean isValidMobType(String type) {
        return EntityList.isRegistered(new ResourceLocation(type));
    }

    @Override
    public void reload() {
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    @Override
    public List<? extends com.sk89q.worldedit.world.World> getWorlds() {
        List<WorldServer> worlds = Arrays.asList(server.worlds);
        List<com.sk89q.worldedit.world.World> ret = new ArrayList<com.sk89q.worldedit.world.World>(worlds.size());
        for (WorldServer world : worlds) {
            ret.add(new CarpetWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof CarpetPlayer) {
            return player;
        } else {
            EntityPlayerMP entity = server.getPlayerList().getPlayerByUsername(player.getName());
            return entity != null ? new CarpetPlayer(entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof CarpetWorld) {
            return world;
        } else {
            for (WorldServer ws : server.worlds) {
                if (ws.getWorldInfo().getWorldName().equals(world.getName())) {
                    return new CarpetWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(Dispatcher dispatcher) {
        if (server == null) return;
        ServerCommandManager mcMan = (ServerCommandManager) server.getCommandManager();

        for (final CommandMapping command : dispatcher.getCommands()) {
            CommandWrapper wrapper = new CommandWrapper(command);
            mcMan.registerCommand(wrapper);
        }
    }

    @Override
    public void registerGameHooks() {
        // We registered the events already anyway, so we just 'turn them on'
        hookingEvents = true;
    }

    @Override
    public PropertiesConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "CarpetMod";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<Capability, Preference>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.PREFER_OTHERS);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<Actor>();
        PlayerList scm = server.getPlayerList();
        for (EntityPlayerMP entity : scm.getPlayers()) {
            if (entity != null) {
                users.add(new CarpetPlayer(entity));
            }
        }
        return users;
    }
}
