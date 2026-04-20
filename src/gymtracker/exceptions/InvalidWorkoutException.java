package gymtracker.exceptions;

/**
 * Thrown when a workout session contains invalid data.
 */
public class InvalidWorkoutException extends Exception {
    public InvalidWorkoutException(String message) {
        super("Invalid workout: " + message);
    }
}
