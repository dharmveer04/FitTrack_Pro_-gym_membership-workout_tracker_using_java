package gymtracker.models;

import gymtracker.interfaces.Exportable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single workout session for a member.
 * Demonstrates: Exportable interface, file-friendly serialisation.
 */
public class WorkoutSession implements Exportable {

    private final String sessionId;
    private final String memberId;
    private final String date;
    private final String workoutType;
    private final int    durationMinutes;
    private final int    caloriesBurned;
    private final String notes;

    /** Constructor used when logging a new workout (auto-generates ID and date). */
    public WorkoutSession(String memberId, String workoutType,
                          int durationMinutes, int caloriesBurned, String notes) {
        this.sessionId       = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.memberId        = memberId;
        this.date            = LocalDate.now().toString();
        this.workoutType     = workoutType;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned  = caloriesBurned;
        this.notes           = notes == null ? "" : notes;
    }

    /** Constructor used when loading from a CSV file. */
    public WorkoutSession(String sessionId, String memberId, String date,
                          String workoutType, int durationMinutes,
                          int caloriesBurned, String notes) {
        this.sessionId       = sessionId;
        this.memberId        = memberId;
        this.date            = date;
        this.workoutType     = workoutType;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned  = caloriesBurned;
        this.notes           = notes == null ? "" : notes;
    }

    // ── Getters ──────────────────────────────────────────────
    public String getSessionId()       { return sessionId; }
    public String getMemberId()        { return memberId; }
    public String getDate()            { return date; }
    public String getWorkoutType()     { return workoutType; }
    public int    getDurationMinutes() { return durationMinutes; }
    public int    getCaloriesBurned()  { return caloriesBurned; }
    public String getNotes()           { return notes; }

    // ── Exportable ───────────────────────────────────────────
    @Override
    public String exportToCSV() {
        // Replace commas in notes with semicolons to keep CSV valid
        return String.join(",",
                sessionId, memberId, date, workoutType,
                String.valueOf(durationMinutes),
                String.valueOf(caloriesBurned),
                notes.replace(",", ";"));
    }

    /** Parse a CSV line produced by exportToCSV(). */
    public static WorkoutSession fromCSV(String csvLine) {
        String[] p = csvLine.split(",", 7);
        return new WorkoutSession(
                p[0], p[1], p[2], p[3],
                Integer.parseInt(p[4]),
                Integer.parseInt(p[5]),
                p.length > 6 ? p[6].replace(";", ",") : "");
    }

    @Override
    public String toString() {
        return String.format("%s | %-15s | %3d min | %4d cal",
                date, workoutType, durationMinutes, caloriesBurned);
    }
}
