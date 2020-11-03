package carpet.launch;

import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;

import java.util.Collection;

@SuppressWarnings("unused")
public class MixinPlatformAgent extends MixinPlatformAgentAbstract implements IMixinPlatformServiceAgent {
    @Override
    public void init() {

    }

    @Override
    public String getSideName() {
        return "SERVER";
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }
}
