package narcolepticfrog.rsmm.events;

public interface TickStartListener {

    /**
     * Gets called at the beginning of each server tick.
     */
    void onTickStart(int tick);

}
