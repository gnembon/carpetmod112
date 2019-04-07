package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PortalCaching {

    public static void portalCachingCleared(World world, int cachedCount, ArrayList<Vec3d> uncacheCount) {
        if(cachedCount == 0 && uncacheCount.size() == 0) return;
        final int count = uncacheCount.size();
        List<ITextComponent> comp = new ArrayList<>();
        LoggerRegistry.getLogger("portalCaching").log( (option) -> {
            comp.add(Messenger.s(null, String.format("%s Portals cached %d, Portal caches removed %d", world.provider.getDimensionType(), cachedCount, count)));
            switch (option) {
                case "brief":
                    return comp.toArray(new ITextComponent[0]);
                case "full":
                    return finalReport(world, comp, uncacheCount).toArray(new ITextComponent[0]);
                default:
                    return null;
            }
        });
    }

    private static List<ITextComponent> finalReport(World world, List<ITextComponent> comp, ArrayList<Vec3d> uncacheCount){
        List<String> line = new ArrayList<>();
        for (int i = 0; i < uncacheCount.size(); i++)
        {
            Vec3d p = uncacheCount.get(i);
            Vec3d pos, tp, mot;
            if(world.provider.getDimensionType().getId() == -1){
                pos = new Vec3d(p.x * 8, p.y, p.z * 8);
                tp = pos.add(4, 0, 4);
                mot = pos.add(8, 0, 8);
            } else {
                pos = new Vec3d(p.x * 0.125, p.y, p.z * 0.125);
                tp = pos.add(0.5, 0, 0.5);
                mot = pos.add(1, 0, 1);
            }
            line.add("w  x");
            line.add(String.format("^w Cache: %d\nx: %f\ny: %f\nz: %f\n------------\nmx: %f\nmy: %f\nmz: %f",
                    i, pos.x, pos.y, pos.z, mot.x, mot.y, mot.z));
            line.add("?/tp " + tp.x +" "+ tp.y +" "+ tp.z);

            if ((((i+1) % 50)==0) || i == uncacheCount.size()-1)
            {
                comp.add(Messenger.m(null, line.toArray(new Object[0])));
                line.clear();
            }
        }
        return comp;
    }
}
