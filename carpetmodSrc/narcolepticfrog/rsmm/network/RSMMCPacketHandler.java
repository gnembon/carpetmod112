package narcolepticfrog.rsmm.network;

public interface RSMMCPacketHandler {

    void handleMeter(RSMMCPacketMeter packet);

    void handleMeterGroup(RSMMCPacketMeterGroup packet);

}
