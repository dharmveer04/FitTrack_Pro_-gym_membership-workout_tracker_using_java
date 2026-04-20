package gymtracker.services;

import gymtracker.exceptions.InvalidWorkoutException;
import gymtracker.exceptions.MemberNotFoundException;
import gymtracker.models.Member;
import gymtracker.models.WorkoutSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business-logic layer for workout sessions.
 * Demonstrates: Custom exception handling, validation, stream operations.
 */
public class WorkoutService {

    private List<WorkoutSession> allSessions;
    private final MemberService memberService;

    public WorkoutService(MemberService memberService) {
        this.allSessions   = new ArrayList<>();
        this.memberService = memberService;
    }

    /**
     * Validate and log a new workout session.
     * @throws InvalidWorkoutException if the session data is invalid.
     * @throws MemberNotFoundException if the member does not exist.
     */
    public void logWorkout(WorkoutSession session)
            throws InvalidWorkoutException, MemberNotFoundException {

        // ── Validation (exception handling demo) ─────────────
        if (session.getWorkoutType() == null || session.getWorkoutType().isBlank()) {
            throw new InvalidWorkoutException("Workout type cannot be empty.");
        }
        if (session.getDurationMinutes() <= 0) {
            throw new InvalidWorkoutException("Duration must be greater than 0 minutes.");
        }
        if (session.getCaloriesBurned() < 0) {
            throw new InvalidWorkoutException("Calories burned cannot be negative.");
        }

        // Lookup member — throws MemberNotFoundException if absent
        Member member = memberService.getMember(session.getMemberId());
        member.addWorkoutSession(session);
        allSessions.add(session);
    }

    public List<WorkoutSession> getWorkoutsForMember(String memberId) {
        return allSessions.stream()
                .filter(s -> s.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    public List<WorkoutSession> getAllWorkouts() {
        return Collections.unmodifiableList(allSessions);
    }

    public List<WorkoutSession> getTodayWorkouts() {
        String today = LocalDate.now().toString();
        return allSessions.stream()
                .filter(s -> s.getDate().equals(today))
                .collect(Collectors.toList());
    }

    public int getTotalCaloriesBurned() {
        return allSessions.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();
    }

    public Map<String, Long> getWorkoutTypeStats() {
        return allSessions.stream()
                .collect(Collectors.groupingBy(WorkoutSession::getWorkoutType, Collectors.counting()));
    }

    /** Bulk-load from persistence; associates sessions with their members. */
    public void setAllSessions(List<WorkoutSession> sessions) {
        this.allSessions = new ArrayList<>(sessions);
        for (WorkoutSession s : sessions) {
            try {
                memberService.getMember(s.getMemberId()).addWorkoutSession(s);
            } catch (MemberNotFoundException ignored) {
                // Session references a deleted member — skip
            }
        }
    }
}
