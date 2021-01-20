package carpet.helpers;

import carpet.mixin.accessors.PlayerActionC2SPacketAccessor;
import carpet.mixin.accessors.EntityAccessor;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import javax.annotation.Nullable;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.Material;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;

public class EntityPlayerActionPack
{
    private ServerPlayerEntity player;

    private boolean doesAttack;
    private int attackInterval;
    private int attackCooldown;

    private boolean doesUse;
    private int useInterval;
    private int useCooldown;

    private boolean doesJump;
    private int jumpInterval;
    private int jumpCooldown;

    private BlockPos currentBlock = new BlockPos(-1,-1,-1);
    private int blockHitDelay;
    private boolean isHittingBlock;
    private float curBlockDamageMP;

    private boolean sneaking;
    private boolean sprinting;
    private float forward;
    private float strafing;

    public EntityPlayerActionPack(ServerPlayerEntity playerIn)
    {
        player = playerIn;
        stop();
    }
    public void copyFrom(EntityPlayerActionPack other)
    {
        doesAttack = other.doesAttack;
        attackInterval = other.attackInterval;
        attackCooldown = other.attackCooldown;

        doesUse = other.doesUse;
        useInterval = other.useInterval;
        useCooldown = other.useCooldown;

        doesJump = other.doesJump;
        jumpInterval = other.jumpInterval;
        jumpCooldown = other.jumpCooldown;


        currentBlock = other.currentBlock;
        blockHitDelay = other.blockHitDelay;
        isHittingBlock = other.isHittingBlock;
        curBlockDamageMP = other.curBlockDamageMP;

        sneaking = other.sneaking;
        sprinting = other.sprinting;
        forward = other.forward;
        strafing = other.strafing;
    }

    public String toString() {
        return (doesAttack ? "t" : "f") + ":" +
                attackInterval + ":" +
                attackCooldown + ":" +
                (doesUse ? "t" : "f") + ":" +
                useInterval + ":" +
                useCooldown + ":" +
                (doesJump ? "t" : "f") + ":" +
                jumpInterval + ":" +
                jumpCooldown + ":" +
                (sneaking ? "t" : "f") + ":" +
                (sprinting ? "t" : "f") + ":" +
                forward + ":" +
                strafing;
    }

    public void fromString(String s){
        String[] list = s.split(":");
        doesAttack = list[0].equals("t");
        attackInterval = Integer.valueOf(list[1]);
        attackCooldown = Integer.valueOf(list[2]);
        doesUse = list[3].equals("t");
        useInterval = Integer.valueOf(list[4]);
        useCooldown = Integer.valueOf(list[5]);
        doesJump = list[6].equals("t");
        jumpInterval = Integer.valueOf(list[7]);
        jumpCooldown = Integer.valueOf(list[8]);
        sneaking = list[9].equals("t");
        sprinting = list[10].equals("t");
        forward = Float.valueOf(list[11]);
        strafing = Float.valueOf(list[12]);
    }

    public EntityPlayerActionPack setAttack(int interval, int offset)
    {
        if (interval < 1)
        {
            CarpetSettings.LOG.error("attack interval needs to be positive");
            return this;
        }
        this.doesAttack = true;
        this.attackInterval = interval;
        this.attackCooldown = interval+offset;
        return this;
    }
    public EntityPlayerActionPack setUse(int interval, int offset)
    {
        if (interval < 1)
        {
            CarpetSettings.LOG.error("use interval needs to be positive");
            return this;
        }
        this.doesUse = true;
        this.useInterval = interval;
        this.useCooldown = interval+offset;
        return this;
    }
    public EntityPlayerActionPack setUseForever()
    {
        this.doesUse = true;
        this.useInterval = 1;
        this.useCooldown = 1;
        return this;
    }
    public EntityPlayerActionPack setAttackForever()
    {
        this.doesAttack = true;
        this.attackInterval = 1;
        this.attackCooldown = 1;
        return this;
    }
    public EntityPlayerActionPack setJump(int interval, int offset)
    {
        if (interval < 1)
        {
            CarpetSettings.LOG.error("jump interval needs to be positive");
            return this;
        }
        this.doesJump = true;
        this.jumpInterval = interval;
        this.jumpCooldown = interval+offset;
        return this;
    }
    public EntityPlayerActionPack setJumpForever()
    {
        this.doesJump = true;
        this.jumpInterval = 1;
        this.jumpCooldown = 1;
        return this;
    }
    public EntityPlayerActionPack setSneaking(boolean doSneak)
    {
        sneaking = doSneak;
        player.setSneaking(doSneak);
        if (sprinting && sneaking)
            setSprinting(false);
        return this;
    }
    public EntityPlayerActionPack setSprinting(boolean doSprint)
    {
        sprinting = doSprint;
        player.setSprinting(doSprint);
        if (sneaking && sprinting)
            setSneaking(false);
        return this;
    }

    public EntityPlayerActionPack setForward(float value)
    {
        forward = value;
        return this;
    }
    public EntityPlayerActionPack setStrafing(float value)
    {
        strafing = value;
        return this;
    }
    public boolean look(String where)
    {
        switch (where)
        {
            case "north":
               look(180.0f,0.0F); return true;
            case "south":
                look (0.0F, 0.0F); return true;
            case "east":
                look(-90.0F, 0.0F); return true;
            case "west":
                look(90.0F, 0.0F); return true;
            case "up":
                look(player.yaw, -90.0F); return true;
            case "down":
                look(player.yaw,  90.0F); return true;
            case "left":
            case "right":
                return turn(where);
        }
        return false;
    }
    public EntityPlayerActionPack look(float yaw, float pitch)
    {
        ((EntityAccessor) player).invokeSetRotation(yaw, MathHelper.clamp(pitch,-90.0F, 90.0F));
        return this;
    }
    public boolean turn(String where)
    {
        switch (where)
        {
            case "left":
                turn(-90.0F,0.0F); return true;
            case "right":
                turn (90.0F, 0.0F); return true;
            case "up":
                turn(0.0F, -5.0F); return true;
            case "down":
                turn(0.0F, 5.0F); return true;
        }
        return false;
    }
    public EntityPlayerActionPack turn(float yaw, float pitch)
    {
        ((EntityAccessor) player).invokeSetRotation(player.yaw+yaw,MathHelper.clamp(player.pitch+pitch,-90.0F, 90.0F));
        return  this;
    }



    public EntityPlayerActionPack stop()
    {
        this.doesUse = false;
        this.doesAttack = false;
        this.doesJump = false;
        resetBlockRemoving();
        setSneaking(false);
        setSprinting(false);
        forward = 0.0F;
        strafing = 0.0F;
        player.setJumping(false);


        return this;
    }

    public void swapHands()
    {
        player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.SWAP_HELD_ITEMS,null, null));
    }

    public void dropItem()
    {
        player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.DROP_ITEM,null, null));
    }
    public void mount()
    {
        List<Entity> entities = player.world.getEntities(
                player,
                player.getBoundingBox().stretch(3.0D, 1.0D, 3.0D),
                other -> !(other instanceof PlayerEntity)
        );
        if (entities.size() == 0)
        {
            return;
        }
        Entity closest = entities.get(0);
        double distance = player.method_34553(closest);
        for (Entity e: entities)
        {
            double dd = player.method_34553(e);
            if (dd<distance)
            {
                distance = dd;
                closest = e;
            }
        }
        player.startRiding(closest,true);
    }
    public void dismount()
    {
        player.stopRiding();
    }

    public void onUpdate()
    {
        if (doesJump)
        {
            if (--jumpCooldown==0)
            {
                jumpCooldown = jumpInterval;
                //jumpOnce();
                player.setJumping(true);
            }
            else
            {
                player.setJumping(false);
            }
        }

        boolean used = false;

        if (doesUse && (--useCooldown)==0)
        {
            useCooldown = useInterval;
            used  = useOnce();
        }
        if (doesAttack)
        {
            if ((--attackCooldown) == 0)
            {
                attackCooldown = attackInterval;
                if (!(used)) attackOnce();
            }
            else
            {
                resetBlockRemoving();
            }
        }
        if (forward != 0.0F)
        {
            //CarpetSettings.LOG.error("moving it forward");
            player.forwardSpeed = forward*(sneaking?0.3F:1.0F);
        }
        if (strafing != 0.0F)
        {
            player.sidewaysSpeed = strafing*(sneaking?0.3F:1.0F);
        }
    }

    public void jumpOnce()
    {
        if (player.onGround)
        {
            player.jump();
        }
    }

    public void attackOnce()
    {
        BlockHitResult raytraceresult = mouseOver();
        if(raytraceresult == null) return;

        switch (raytraceresult.field_26673)
        {
            case ENTITY:
                player.attack(raytraceresult.field_26676);
                this.player.swingHand(Hand.MAIN_HAND);
                break;
            case MISS:
                break;
            case BLOCK:
                BlockPos blockpos = raytraceresult.getBlockPos();
                if (player.method_29608().getBlockState(blockpos).getMaterial() != Material.AIR)
                {
                    onPlayerDamageBlock(blockpos,raytraceresult.field_26674.getOpposite());
                    this.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
        }
    }

    public boolean useOnce()
    {
        BlockHitResult raytraceresult = mouseOver();
        for (Hand enumhand : Hand.values())
        {
            ItemStack itemstack = this.player.getStackInHand(enumhand);
            if (raytraceresult != null)
            {
                switch (raytraceresult.field_26673)
                {
                    case ENTITY:
                        Entity target = raytraceresult.field_26676;
                        Vec3d vec3d = new Vec3d(raytraceresult.field_26675.x - target.field_33071, raytraceresult.field_26675.y - target.field_33072, raytraceresult.field_26675.z - target.field_33073);

                        boolean flag = player.method_34630(target);
                        double d0 = 36.0D;

                        if (!flag)
                        {
                            d0 = 9.0D;
                        }

                        if (player.method_34553(target) < d0)
                        {
                            ActionResult res = player.interact(target,enumhand);
                            if (res == ActionResult.SUCCESS)
                            {
                                return true;
                            }
                            res = target.interactAt(player, vec3d, enumhand);
                            if (res == ActionResult.SUCCESS)
                            {
                                return true;
                            }
                        }
                        break;
                    case MISS:
                        break;
                    case BLOCK:
                        BlockPos blockpos = raytraceresult.getBlockPos();

                        if (player.method_29608().getBlockState(blockpos).getMaterial() != Material.AIR)
                        {
                            if(itemstack.isEmpty())
                                continue;
                            float x = (float) raytraceresult.field_26675.x;
                            float y = (float) raytraceresult.field_26675.y;
                            float z = (float) raytraceresult.field_26675.z;

                            ActionResult res = player.interactionManager.interactBlock(player, player.method_29608(), itemstack, enumhand, blockpos, raytraceresult.field_26674, x, y, z);
                            if (res == ActionResult.SUCCESS)
                            {
                                this.player.swingHand(enumhand);
                                return true;
                            }
                        }
                }
            }
            ActionResult res = player.interactionManager.interactItem(player,player.method_29608(),itemstack,enumhand);
            if (res == ActionResult.SUCCESS)
            {
                return true;
            }
        }
        return false;
    }

    private BlockHitResult rayTraceBlocks(double blockReachDistance)
    {
        Vec3d eyeVec = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.method_34535(1.0F);
        Vec3d pointVec = eyeVec.add(lookVec.x * blockReachDistance, lookVec.y * blockReachDistance, lookVec.z * blockReachDistance);
        return player.method_29608().rayTrace(eyeVec, pointVec, false, false, true);
    }

    public BlockHitResult mouseOver()
    {
        World world = player.method_29608();
        if (world == null)
            return null;
        BlockHitResult result = null;

        Entity pointedEntity = null;
        double reach = player.isCreative() ? 5.0D : 4.5D;
        result = rayTraceBlocks(reach);
        Vec3d eyeVec = player.getCameraPosVec(1.0F);
        boolean flag = !player.isCreative();
        if (player.isCreative()) reach = 6.0D;
        double extendedReach = reach;

        if (result != null)
        {
            extendedReach = result.field_26675.distanceTo(eyeVec);
            if (world.getBlockState(result.getBlockPos()).getMaterial() == Material.AIR)
                result = null;
        }

        Vec3d lookVec = player.method_34535(1.0F);
        Vec3d pointVec = eyeVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        Vec3d field_26675 = null;
        List<Entity> list = world.getEntities(
                player,
                player.getBoundingBox().stretch(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach).expand(1.0D, 1.0D, 1.0D),
                Predicates.and(EntityPredicates.EXCEPT_SPECTATOR, new Predicate<Entity>() {
                        public boolean apply(@Nullable Entity entity)
                        {
                            return entity != null && entity.collides();
                        }
                })
        );
        double d2 = extendedReach;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = list.get(j);
            Box axisalignedbb = entity1.getBoundingBox().expand(entity1.getTargetingMargin());
            BlockHitResult raytraceresult = axisalignedbb.method_28530(eyeVec, pointVec);

            if (axisalignedbb.method_28529(eyeVec))
            {
                if (d2 >= 0.0D)
                {
                    pointedEntity = entity1;
                    field_26675 = raytraceresult == null ? eyeVec : raytraceresult.field_26675;
                    d2 = 0.0D;
                }
            }
            else if (raytraceresult != null)
            {
                double d3 = eyeVec.distanceTo(raytraceresult.field_26675);

                if (d3 < d2 || d2 == 0.0D)
                {
                    if (entity1.getRootVehicle() == player.getRootVehicle())
                    {
                        if (d2 == 0.0D)
                        {
                            pointedEntity = entity1;
                            field_26675 = raytraceresult.field_26675;
                        }
                    }
                    else
                    {
                        pointedEntity = entity1;
                        field_26675 = raytraceresult.field_26675;
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && flag && eyeVec.distanceTo(field_26675) > 3.0D)
        {
            pointedEntity = null;
            result = new BlockHitResult(BlockHitResult.Type.MISS, field_26675, null, new BlockPos(field_26675));
        }

        if (pointedEntity != null && (d2 < extendedReach || result == null))
        {
            result = new BlockHitResult(pointedEntity, field_26675);
        }

        return result;
    }

    public boolean clickBlock(BlockPos loc, Direction face) // don't call this one
    {
        World world = player.method_29608();
        if (player.interactionManager.getGameMode()!=GameMode.ADVENTURE)
        {
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR)
            {
                return false;
            }

            if (!player.abilities.allowModifyWorld)
            {
                ItemStack itemstack = player.getMainHandStack();

                if (itemstack.isEmpty())
                {
                    return false;
                }

                if (!itemstack.canDestroy(world.getBlockState(loc).getBlock()))
                {
                    return false;
                }
            }
        }

        if (!world.getWorldBorder().contains(loc))
        {
            return false;
        }
        else
        {
            if (player.interactionManager.getGameMode()==GameMode.CREATIVE)
            {
                player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, loc, face));
                clickBlockCreative(world, loc, face);
                this.blockHitDelay = 5;
            }
            else if (!this.isHittingBlock || !(currentBlock.equals(loc)))
            {
                if (this.isHittingBlock)
                {
                    player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
                }

                BlockState iblockstate = world.getBlockState(loc);
                player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, loc, face));
                boolean flag = iblockstate.getMaterial() != Material.AIR;

                if (flag && this.curBlockDamageMP == 0.0F)
                {
                    iblockstate.getBlock().onBlockBreakStart(world, loc, player);
                }

                if (flag && iblockstate.method_27178(player, world, loc) >= 1.0F)
                {
                    this.onPlayerDestroyBlock(loc);
                }
                else
                {
                    this.isHittingBlock = true;
                    this.currentBlock = loc;
                    this.curBlockDamageMP = 0.0F;
                    world.method_26094(player.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    private void clickBlockCreative(World world, BlockPos pos, Direction facing)
    {
        if (!world.extinguishFire(player, pos, facing))
        {
            onPlayerDestroyBlock(pos);
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, Direction directionFacing) //continue clicking - one to call
    {
        if (this.blockHitDelay > 0)
        {
            --this.blockHitDelay;
            return true;
        }
        World world = player.method_29608();
        if (player.interactionManager.getGameMode()==GameMode.CREATIVE && world.getWorldBorder().contains(posBlock))
        {
            this.blockHitDelay = 5;
            player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(world, posBlock, directionFacing);
            return true;
        }
        else if (posBlock.equals(currentBlock))
        {
            BlockState iblockstate = world.getBlockState(posBlock);

            if (iblockstate.getMaterial() == Material.AIR)
            {
                this.isHittingBlock = false;
                return false;
            }
            else
            {
                this.curBlockDamageMP += iblockstate.method_27178(player, world, posBlock);

                if (this.curBlockDamageMP >= 1.0F)
                {
                    this.isHittingBlock = false;
                    player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    this.onPlayerDestroyBlock(posBlock);
                    this.curBlockDamageMP = 0.0F;
                    this.blockHitDelay = 5;
                }
                //player.getEntityId()
                //send to all, even the breaker
                world.method_26094(-1, this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        }
        else
        {
            return this.clickBlock(posBlock, directionFacing);
        }
    }

    private boolean onPlayerDestroyBlock(BlockPos pos)
    {
        World world = player.method_29608();
        if (player.interactionManager.getGameMode()!=GameMode.ADVENTURE)
        {
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR)
            {
                return false;
            }

            if (player.abilities.allowModifyWorld)
            {
                ItemStack itemstack = player.getMainHandStack();

                if (itemstack.isEmpty())
                {
                    return false;
                }

                if (!itemstack.canDestroy(world.getBlockState(pos).getBlock()))
                {
                    return false;
                }
            }
        }

        if (player.interactionManager.getGameMode()==GameMode.CREATIVE && !player.getMainHandStack().isEmpty() && player.getMainHandStack().getItem() instanceof SwordItem)
        {
            return false;
        }
        else
        {
            BlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if ((block instanceof CommandBlock || block instanceof StructureBlock) && !player.isCreativeLevelTwoOp())
            {
                return false;
            }
            else if (iblockstate.getMaterial() == Material.AIR)
            {
                return false;
            }
            else
            {
                world.method_26069(2001, pos, Block.getRawIdFromState(iblockstate));
                block.onBreak(world, pos, iblockstate, player);
                boolean flag = world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

                if (flag)
                {
                    block.method_26439(world, pos, iblockstate);
                }

                this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                if (!(player.interactionManager.getGameMode()==GameMode.CREATIVE))
                {
                    ItemStack itemstack1 = player.getMainHandStack();

                    if (!itemstack1.isEmpty())
                    {
                        itemstack1.postMine(world, iblockstate, pos, player);

                        if (itemstack1.isEmpty())
                        {
                            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }

                return flag;
            }
        }
    }

    public void resetBlockRemoving()
    {
        if (this.isHittingBlock)
        {
            player.networkHandler.onPlayerAction(createDiggingPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBlock, Direction.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            player.method_29608().method_26094(player.getEntityId(), this.currentBlock, -1);
            player.resetLastAttackedTicks();
            this.currentBlock = new BlockPos(-1,-1,-1);
        }
    }


    /*
    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand vec)
    {
        this.syncCurrentPlayItem();
        ItemStack itemstack = player.getHeldItem(vec);
        float f = (float)(facing.xCoord - (double)stack.getX());
        float f1 = (float)(facing.yCoord - (double)stack.getY());
        float f2 = (float)(facing.zCoord - (double)stack.getZ());
        boolean flag = false;

        if (!this.mc.world.getWorldBorder().contains(stack))
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            if (this.currentGameType != GameType.SPECTATOR)
            {
                IBlockState iblockstate = worldIn.getBlockState(stack);

                if ((!player.isSneaking() || player.getHeldItemMainhand().func_190926_b() && player.getHeldItemOffhand().func_190926_b()) && iblockstate.getBlock().onBlockActivated(worldIn, stack, iblockstate, player, vec, pos, f, f1, f2))
                {
                    flag = true;
                }

                if (!flag && itemstack.getItem() instanceof ItemBlock)
                {
                    ItemBlock itemblock = (ItemBlock)itemstack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, stack, pos, player, itemstack))
                    {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(stack, pos, vec, f, f1, f2));

            if (!flag && this.currentGameType != GameType.SPECTATOR)
            {
                if (itemstack.func_190926_b())
                {
                    return EnumActionResult.PASS;
                }
                else if (player.getCooldownTracker().hasCooldown(itemstack.getItem()))
                {
                    return EnumActionResult.PASS;
                }
                else
                {
                    if (itemstack.getItem() instanceof ItemBlock && !player.canUseCommandBlock())
                    {
                        Block block = ((ItemBlock)itemstack.getItem()).getBlock();

                        if (block instanceof BlockCommandBlock || block instanceof BlockStructure)
                        {
                            return EnumActionResult.FAIL;
                        }
                    }

                    if (this.currentGameType.isCreative())
                    {
                        int i = itemstack.getMetadata();
                        int j = itemstack.func_190916_E();
                        EnumActionResult enumactionresult = itemstack.onItemUse(player, worldIn, stack, vec, pos, f, f1, f2);
                        itemstack.setItemDamage(i);
                        itemstack.func_190920_e(j);
                        return enumactionresult;
                    }
                    else
                    {
                        return itemstack.onItemUse(player, worldIn, stack, vec, pos, f, f1, f2);
                    }
                }
            }
            else
            {
                return EnumActionResult.SUCCESS;
            }
        }
    }

    public EnumActionResult processRightClick(EntityPlayer player, World worldIn, EnumHand stack)
    {
        if (this.currentGameType == GameType.SPECTATOR)
        {
            return EnumActionResult.PASS;
        }
        else
        {
            this.syncCurrentPlayItem();
            this.connection.sendPacket(new CPacketPlayerTryUseItem(stack));
            ItemStack itemstack = player.getHeldItem(stack);

            if (player.getCooldownTracker().hasCooldown(itemstack.getItem()))
            {
                return EnumActionResult.PASS;
            }
            else
            {
                int i = itemstack.func_190916_E();
                ActionResult<ItemStack> actionresult = itemstack.useItemRightClick(worldIn, player, stack);
                ItemStack itemstack1 = actionresult.getResult();

                if (itemstack1 != itemstack || itemstack1.func_190916_E() != i)
                {
                    player.setHeldItem(stack, itemstack1);
                }

                return actionresult.getType();
            }
        }
    }

    public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, EnumHand heldItem)
    {
        this.syncCurrentPlayItem();
        this.connection.sendPacket(new CPacketUseEntity(target, heldItem));
        return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : player.func_190775_a(target, heldItem);
    }

    /
     * Handles right clicking an entity from the entities side, sends a packet to the server.
     *
    public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, RayTraceResult raytrace, EnumHand heldItem)
    {
        this.syncCurrentPlayItem();
        Vec3d vec3d = new Vec3d(raytrace.field_26675.xCoord - target.posX, raytrace.field_26675.yCoord - target.posY, raytrace.field_26675.zCoord - target.posZ);
        this.connection.sendPacket(new CPacketUseEntity(target, heldItem, vec3d));
        return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : target.applyPlayerInteraction(player, vec3d, heldItem);
    }
*/

    private static PlayerActionC2SPacket createDiggingPacket(PlayerActionC2SPacket.Action action, BlockPos pos, Direction facing) {
        PlayerActionC2SPacket p = new PlayerActionC2SPacket();
        PlayerActionC2SPacketAccessor acc = (PlayerActionC2SPacketAccessor) p;
        acc.setAction(action);
        acc.setPos(pos);
        acc.setDirection(facing);
        return p;
    }
}
