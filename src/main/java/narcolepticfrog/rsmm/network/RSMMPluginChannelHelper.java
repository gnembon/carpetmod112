package narcolepticfrog.rsmm.network;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class RSMMPluginChannelHelper {
    private final static String CHANNEL_SEPARATOR = "\u0000";
    public final static String REGISTER_CHANNELS = "REGISTER";
    public final static String UNREGISTER_CHANNELS = "UNREGISTER";

    public static List<String> getChannels(PacketBuffer buff) {
        buff.resetReaderIndex();
        byte[] bytes = new byte[buff.readableBytes()];
        buff.readBytes(bytes);
        String channelString = new String(bytes, Charsets.UTF_8);
        List<String> channels = Lists.newArrayList(channelString.split(CHANNEL_SEPARATOR));
        return channels;
    }
}
