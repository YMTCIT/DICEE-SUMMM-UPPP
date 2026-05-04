import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardManager {

    private static final String FILE_PATH = "leaderboard.dat";

    private LeaderboardManager() {}

    /** Append a new KhaiGu-victor entry and persist the full list. */
    public static void saveEntry(LeaderboardEntry entry) {
        List<LeaderboardEntry> entries = loadAllEntries();
        entries.add(entry);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FILE_PATH))) {
            oos.writeObject(entries);
        } catch (IOException e) {
            System.err.println("Leaderboard save failed: " + e.getMessage());
        }
    }

    /** Load all entries sorted: coins desc, then battlesWon desc. */
    @SuppressWarnings("unchecked")
    public static List<LeaderboardEntry> loadAllEntries() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            List<LeaderboardEntry> list = (List<LeaderboardEntry>) ois.readObject();
            list.sort(Comparator
                    .comparingInt(LeaderboardEntry::getCoins).reversed()
                    .thenComparingInt(LeaderboardEntry::getBattlesWon).reversed());
            return list;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Leaderboard load failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Return at most the top N entries. */
    public static List<LeaderboardEntry> getTopN(int n) {
        List<LeaderboardEntry> all = loadAllEntries();
        return all.subList(0, Math.min(n, all.size()));
    }
}
