package gymtracker.interfaces;

/**
 * Interface for tracking fitness progress of members.
 */
public interface Trackable {
    String trackProgress();
    String getProgressReport();
    int getTotalWorkouts();
    int getTotalCaloriesBurned();
}
