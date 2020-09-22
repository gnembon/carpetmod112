package carpet.utils.extensions;

public interface DupingPlayer {
    void clearDupeItem();
    void dupeItem(int slot);
    int getDupeItem();
    void dupeItemScan(boolean scanForDuping);
}
