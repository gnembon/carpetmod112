package carpet.worldedit;

import com.sk89q.worldedit.world.World;

final class CarpetAdapter {

    private CarpetAdapter() {
    }

    public static World adapt(net.minecraft.world.World world) {
        return new CarpetWorld(world);
    }

}
