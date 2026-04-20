package gymtracker.filehandling;

import gymtracker.models.Member;
import gymtracker.models.WorkoutSession;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O for the application using CSV files.
 * Demonstrates: File handling with BufferedReader/Writer, IOException handling.
 */
public class DataManager {

    private static final String DATA_DIR     = "data";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.csv";
    private static final String WORKOUTS_FILE = DATA_DIR + "/workouts.csv";

    public DataManager() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("[DataManager] Warning: could not create data directory: " + e.getMessage());
        }
    }

    // ── Members ──────────────────────────────────────────────

    public void saveMembers(List<Member> members) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            bw.write("id,name,email,phone,membershipType,joinDate,monthlyFee,isPremium,trainer,nutritionPlan");
            bw.newLine();
            for (Member m : members) {
                bw.write(m.exportToCSV());
                bw.newLine();
            }
        }
    }

    public List<Member> loadMembers() throws IOException {
        List<Member> result = new ArrayList<>();
        File f = new File(MEMBERS_FILE);
        if (!f.exists()) return result;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        result.add(Member.fromCSV(line));
                    } catch (Exception e) {
                        System.err.println("[DataManager] Skipping bad member line: " + line);
                    }
                }
            }
        }
        return result;
    }

    // ── Workouts ─────────────────────────────────────────────

    public void saveWorkouts(List<WorkoutSession> sessions) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(WORKOUTS_FILE))) {
            bw.write("sessionId,memberId,date,workoutType,durationMinutes,caloriesBurned,notes");
            bw.newLine();
            for (WorkoutSession s : sessions) {
                bw.write(s.exportToCSV());
                bw.newLine();
            }
        }
    }

    public List<WorkoutSession> loadWorkouts() throws IOException {
        List<WorkoutSession> result = new ArrayList<>();
        File f = new File(WORKOUTS_FILE);
        if (!f.exists()) return result;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        result.add(WorkoutSession.fromCSV(line));
                    } catch (Exception e) {
                        System.err.println("[DataManager] Skipping bad workout line: " + line);
                    }
                }
            }
        }
        return result;
    }
}
