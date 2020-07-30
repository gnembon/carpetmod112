package carpet.launch;

import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import java.util.Collection;
import java.util.Collections;

public class MixinServiceCarpetMod extends MixinServiceLaunchWrapper {
    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singleton("carpet.launch.MixinPlatformAgent");
    }
}
