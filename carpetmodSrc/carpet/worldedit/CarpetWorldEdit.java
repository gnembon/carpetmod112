package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.LocalWorldAdapter;

import carpet.CarpetSettings;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Carpet implementation of WorldEdit.
 */
@SuppressWarnings("deprecation")
class CarpetWorldEdit {

    public static Logger logger;
    public static final String MOD_ID = "worldedit";
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";

    public static CarpetWorldEdit inst = new CarpetWorldEdit();

    private CarpetPlatform platform;
    private CarpetConfiguration config;
    
    private boolean firstTick = true;

    public void onServerLoaded(MinecraftServer server) {
        logger = LogManager.getLogger("WorldEdit");

        config = new CarpetConfiguration(new File("worldedit.properties").getAbsoluteFile());
        config.load();
        
        CarpetBiomeRegistry.populate();
        
        this.platform = new CarpetPlatform(this);
        
        WorldEdit.getInstance().getPlatformManager().register(platform);
    }
    
    public void onStartTick() {
        ThreadSafeCache.getInstance().tickStart();
        if (firstTick) {
            firstTick = false;
            WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
        }
    }

    public void onCommand(ICommand command, ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP) {
            if (((EntityPlayerMP) sender).world.isRemote) return;
            String[] split = new String[args.length + 1];
            System.arraycopy(args, 0, split, 1, args.length);
            split[0] = command.getName();
            com.sk89q.worldedit.event.platform.CommandEvent weEvent =
                    new com.sk89q.worldedit.event.platform.CommandEvent(wrap((EntityPlayerMP) sender), Joiner.on(" ").join(split));
            WorldEdit.getInstance().getEventBus().post(weEvent);
        }
    }
    
    public boolean onLeftClickBlock(World world, BlockPos pos, EntityPlayerMP player) {
        if (!platform.isHookingEvents())
            return true;
        
        WorldEdit we = WorldEdit.getInstance();
        CarpetPlayer carpetPlayer = wrap(player);
        CarpetWorld carpetWorld = getWorld(world);
        WorldVector vector = new WorldVector(LocalWorldAdapter.adapt(carpetWorld), pos.getX(), pos.getY(), pos.getZ());
        
        boolean result = true;
        
        if (we.handleBlockLeftClick(carpetPlayer, vector))
            result = false;
        
        if (we.handleArmSwing(carpetPlayer))
            result = false;
        
        return result;
    }
    
    public boolean onRightClickBlock(World world, BlockPos pos, EntityPlayerMP player) {
        if (!platform.isHookingEvents())
            return true;
        
        WorldEdit we = WorldEdit.getInstance();
        CarpetPlayer carpetPlayer = wrap(player);
        CarpetWorld carpetWorld = getWorld(world);
        WorldVector vector = new WorldVector(LocalWorldAdapter.adapt(carpetWorld), pos.getX(), pos.getY(), pos.getZ());
        
        boolean result = true;
        
        if (we.handleBlockRightClick(carpetPlayer, vector))
            result = false;
        
        if (we.handleRightClick(carpetPlayer))
            result = false;
        
        return result;
    }
    
    public boolean onRightClickAir(World world, EntityPlayerMP player) {
        if (!platform.isHookingEvents())
            return true;
        
        WorldEdit we = WorldEdit.getInstance();
        CarpetPlayer carpetPlayer = wrap(player);
        
        if (we.handleRightClick(carpetPlayer))
            return false;
        
        return true;
    }

    public static ItemStack toCarpetItemStack(BaseItemStack item) {
        ItemStack ret = new ItemStack(Item.getItemById(item.getType()), item.getAmount(), item.getData());
        for (Map.Entry<Integer, Integer> entry : item.getEnchantments().entrySet()) {
            ret.addEnchantment(Enchantment.getEnchantmentByID((entry.getKey())), entry.getValue());
        }

        return ret;
    }

    /**
     * Get the configuration.
     *
     * @return the Properties configuration
     */
    CarpetConfiguration getConfig() {
        return this.config;
    }

    /**
     * Get the WorldEdit proxy for the given player.
     *
     * @param player the player
     * @return the WorldEdit player
     */
    public CarpetPlayer wrap(EntityPlayerMP player) {
        checkNotNull(player);
        return new CarpetPlayer(player);
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(EntityPlayerMP player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(wrap(player));
    }

    /**
     * Get the WorldEdit proxy for the given world.
     *
     * @param world the world
     * @return the WorldEdit world
     */
    public CarpetWorld getWorld(World world) {
        checkNotNull(world);
        return new CarpetWorld(world);
    }

    /**
     * Get the WorldEdit proxy for the platform.
     *
     * @return the WorldEdit platform
     */
    public Platform getPlatform() {
        return this.platform;
    }

    /**
     * Get the version of the WorldEdit-for-Carpet implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return CarpetSettings.carpetVersion;
    }

}
