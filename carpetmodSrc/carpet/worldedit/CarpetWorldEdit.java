package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.EntityCreate;
import com.sk89q.worldedit.history.change.EntityRemove;
import com.sk89q.worldedit.internal.LocalWorldAdapter;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
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
    
    private EditSession editSession = null;
    private int sessionNestedDepth = 0;

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
    
    public void startEditSession(EntityPlayerMP player) {
        if (player == null)
            return;

        sessionNestedDepth++;
        if (editSession == null) {
            CarpetPlayer carpetPlayer = wrap(player);
            editSession = WorldEdit.getInstance().getSessionManager().get(carpetPlayer).createEditSession(carpetPlayer);
        }
    }
    
    public void finishEditSession(EntityPlayerMP player) {
        if (player == null)
            return;
        
        if (editSession == null) {
            throw new IllegalStateException("Not started!");
        }
        
        sessionNestedDepth--;
        
        if (sessionNestedDepth == 0) {
            CarpetPlayer carpetPlayer = wrap(player);
            if (editSession.getChangeSet().size() > 0)
                WorldEdit.getInstance().getSessionManager().get(carpetPlayer).remember(editSession);
            editSession = null;
        }
    }
    
    public void recordBlockEdit(EntityPlayerMP player, World world, BlockPos pos, IBlockState newBlock, NBTTagCompound newTileEntity) {
        if (player == null)
            return;
        
        if (editSession == null) {
            throw new IllegalStateException("Not started!");
        }
        
        BlockVector position = new BlockVector(pos.getX(), pos.getY(), pos.getZ());
        
        IBlockState oldBlock = world.getBlockState(pos);
        int oldBlockId = Block.getIdFromBlock(oldBlock.getBlock());
        int oldMeta = oldBlock.getBlock().getMetaFromState(oldBlock);
        TileEntity oldTileEntity = world.getTileEntity(pos);
        BaseBlock previous;
        if (oldTileEntity == null)
            previous = new BaseBlock(oldBlockId, oldMeta);
        else
            previous = new BaseBlock(oldBlockId, oldMeta, NBTConverter.fromNative(oldTileEntity.writeToNBT(new NBTTagCompound())));
        
        int newBlockId = Block.getIdFromBlock(newBlock.getBlock());
        int newMeta = newBlock.getBlock().getMetaFromState(newBlock);
        BaseBlock current;
        if (newTileEntity == null)
            current = new BaseBlock(newBlockId, newMeta);
        else
            current = new BaseBlock(newBlockId, newMeta, NBTConverter.fromNative(newTileEntity));
        
        editSession.getChangeSet().add(new BlockChange(position, previous, current));
    }
    
    public void recordEntityCreation(EntityPlayerMP player, World world, Entity created) {
        if (player == null)
            return;
        
        if (editSession == null) {
            throw new IllegalStateException("Not started!");
        }
        
        CarpetEntity carpetEntity = new CarpetEntity(created);
        String entityId = EntityList.getKey(created).toString();
        CompoundTag tag = NBTConverter.fromNative(created.writeToNBT(new NBTTagCompound()));
        BaseEntity baseEntity = new BaseEntity(entityId, tag);
        
        editSession.getChangeSet().add(new EntityCreate(carpetEntity.getLocation(), baseEntity, carpetEntity));
    }
    
    public void recordEntityRemoval(EntityPlayerMP player, World world, Entity removed) {
        if (player == null)
            return;
        
        if (editSession == null) {
            throw new IllegalStateException("Not started!");
        }
        
        CarpetEntity carpetEntity = new CarpetEntity(removed);
        String entityId = EntityList.getKey(removed).toString();
        CompoundTag tag = NBTConverter.fromNative(removed.writeToNBT(new NBTTagCompound()));
        BaseEntity baseEntity = new BaseEntity(entityId, tag);
        
        editSession.getChangeSet().add(new EntityRemove(carpetEntity.getLocation(), baseEntity));
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
