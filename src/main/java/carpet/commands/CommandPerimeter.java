package carpet.commands;

import carpet.utils.Messenger;
import carpet.utils.PerimeterDiagnostics;
import net.minecraft.class_2010;
import net.minecraft.class_2245;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandPerimeter extends CommandCarpetBase
{
    @Override
    public String method_29277()
    {
        return "perimetercheck";
    }

    @Override
    public String method_29275(class_2010 sender)
    {
        return "/perimetercheck <X> <Y> <Z> <target_entity?>";
    }

    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandPerimeterInfo", sender)) return;
        if (args.length < 1)
        {
            throw new class_6182(method_29275(sender));
        }
        else
        {
            String s;
            BlockPos blockpos = sender.method_29606();
            Vec3d vec3d = sender.method_29607();
            double d0 = vec3d.x;
            double d1 = vec3d.y;
            double d2 = vec3d.z;
            if (args.length >= 3)
            {
                d0 = method_28734(d0, args[0], true);
                d1 = method_28734(d1, args[1], false);
                d2 = method_28734(d2, args[2], true);
                blockpos = new BlockPos(d0, d1, d2);
            }
            World world = sender.method_29608();
            CompoundTag nbttagcompound = new CompoundTag();
            MobEntity entityliving = null;
            if (args.length >= 4)
            {
                s = args[3];
                nbttagcompound.putString("id", s);
                entityliving = (MobEntity) ThreadedAnvilChunkStorage.method_27469(nbttagcompound, world, d0, d1+2, d2, true);
                if (entityliving == null)
                {
                    throw new class_6175("Failed to test entity");
                }
            }
            PerimeterDiagnostics.Result res = PerimeterDiagnostics.countSpots((ServerWorld) world, blockpos, entityliving);
            if (sender instanceof PlayerEntity)
            {
                Messenger.m(sender, "w Spawning spaces around ",Messenger.tp("b",blockpos));
            }
            method_28710(sender, this, "Spawn spaces:");
            method_28710(sender, this, String.format("  potential in-liquid: %d",res.liquid));
            method_28710(sender, this, String.format("  potential on-ground: %d",res.ground));
            if (entityliving != null)
            {
                method_28710(sender, this, String.format("  %s: %d",entityliving.getDisplayName().method_32275(),res.specific));
                if (sender instanceof PlayerEntity)
                {
                    res.samples.forEach(bp -> Messenger.m(sender, "w   ", Messenger.tp("w", bp)));
                }
                else
                {
                    res.samples.forEach(bp -> method_28710(sender, this, String.format("    [ %d, %d, %d ]", bp.getX(),bp.getY(),bp.getZ())));
                }
                entityliving.remove();
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 4)
        {
            return method_28731(args, class_2245.method_34587());
        }
        else
        {
            return args.length > 0 && args.length <= 3 ? method_28730(args, 0, pos) : Collections.emptyList();
        }
    }
}

