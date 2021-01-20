package carpet.mixin.pistonClippingFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPlayerPistonClipping;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ExtendedPlayerPistonClipping {
    private int pistonClippingCounter;

    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Override
    public int getClippingCounter() {
        return pistonClippingCounter;
    }

    @Override
    public void setClippingCounter(int counter) {
        this.pistonClippingCounter = counter;
    }

    @Override
    public void move(MovementType type, double x, double y, double z) {
        if (CarpetSettings.pistonClippingFix > 0) {
            this.pistonClippingCounter = CarpetSettings.pistonClippingFix;
        }
        super.move(type, x, y, z);
    }
}
