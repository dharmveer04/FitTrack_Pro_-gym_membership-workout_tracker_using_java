package gymtracker.models;

import gymtracker.interfaces.Exportable;
import gymtracker.interfaces.Trackable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Regular gym member.
 * Demonstrates:
 *   - Inheritance  : extends Person
 *   - Interfaces   : implements Trackable + Exportable
 *   - Enum         : MembershipType with per-tier fee
 */
public class Member extends Person implements Trackable, Exportable {

    // ── Membership tiers ─────────────────────────────────────
    public enum MembershipType {
        BASIC(29.99),
        STANDARD(49.99),
        PREMIUM(79.99);

        private final double fee;
        MembershipType(double fee) { this.fee = fee; }
        public double getFee()     { return fee; }
    }

    // ── Fields ───────────────────────────────────────────────
    protected MembershipType    membershipType;
    protected String            joinDate;
    protected double            monthlyFee;
    protected List<WorkoutSession> workoutSessions;
    protected boolean           active;

    // ── Constructor ──────────────────────────────────────────
    public Member(String id, String name, String email, String phone,
                  MembershipType membershipType, String joinDate) {
        super(id, name, email, phone);
        this.membershipType  = membershipType;
        this.joinDate        = joinDate;
        this.monthlyFee      = membershipType.getFee();
        this.workoutSessions = new ArrayList<>();
        this.active          = true;
    }

    // ── Getters / setters ────────────────────────────────────
    public MembershipType        getMembershipType()  { return membershipType; }
    public String                getJoinDate()        { return joinDate; }
    public double                getMonthlyFee()      { return monthlyFee; }
    public List<WorkoutSession>  getWorkoutSessions() { return workoutSessions; }
    public boolean               isActive()           { return active; }

    public void setMembershipType(MembershipType t) {
        this.membershipType = t;
        this.monthlyFee     = t.getFee();
    }
    public void setActive(boolean active) { this.active = active; }

    public void addWorkoutSession(WorkoutSession s) { workoutSessions.add(s); }

    // ── Person abstract methods ──────────────────────────────
    @Override
    public String getRole() { return "Member"; }

    @Override
    public String getSummary() {
        return String.format("ID: %s | Name: %-20s | Type: %-8s | Joined: %s | Active: %s",
                id, name, membershipType, joinDate, active ? "Yes" : "No");
    }

    // ── Trackable ────────────────────────────────────────────
    @Override
    public String trackProgress() {
        int totalMin = workoutSessions.stream().mapToInt(WorkoutSession::getDurationMinutes).sum();
        return String.format("%s has completed %d workouts, burned %d cal, spent %d minutes exercising.",
                name, getTotalWorkouts(), getTotalCaloriesBurned(), totalMin);
    }

    @Override
    public String getProgressReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Progress Report: ").append(name).append(" ===\n");
        sb.append("Membership : ").append(membershipType).append("\n");
        sb.append("Join Date  : ").append(joinDate).append("\n");
        sb.append("Total Workouts     : ").append(getTotalWorkouts()).append("\n");
        sb.append("Total Calories     : ").append(getTotalCaloriesBurned()).append(" cal\n");
        int totalMin = workoutSessions.stream().mapToInt(WorkoutSession::getDurationMinutes).sum();
        sb.append("Total Time         : ").append(totalMin).append(" min\n\n");

        if (!workoutSessions.isEmpty()) {
            sb.append("Workout Breakdown:\n");
            Map<String, Long> counts = workoutSessions.stream()
                    .collect(Collectors.groupingBy(WorkoutSession::getWorkoutType, Collectors.counting()));
            counts.forEach((type, cnt) ->
                    sb.append("  ").append(type).append(" : ").append(cnt).append(" session(s)\n"));
        }
        return sb.toString();
    }

    @Override
    public int getTotalWorkouts()      { return workoutSessions.size(); }

    @Override
    public int getTotalCaloriesBurned() {
        return workoutSessions.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();
    }

    // ── Exportable ───────────────────────────────────────────
    @Override
    public String exportToCSV() {
        return String.join(",",
                id, name, email, phone,
                membershipType.name(), joinDate,
                String.valueOf(monthlyFee),
                "false", "", "");
    }

    /**
     * Reconstruct a Member (or PremiumMember) from a CSV line.
     * Format: id,name,email,phone,membershipType,joinDate,monthlyFee,isPremium,trainer,nutritionPlan
     */
    public static Member fromCSV(String csvLine) {
        String[] p = csvLine.split(",", 10);
        boolean isPremium = Boolean.parseBoolean(p[7]);
        if (isPremium) {
            return new PremiumMember(
                    p[0], p[1], p[2], p[3],
                    MembershipType.valueOf(p[4]), p[5],
                    p.length > 8 ? p[8] : "",
                    p.length > 9 ? p[9] : "");
        }
        return new Member(p[0], p[1], p[2], p[3], MembershipType.valueOf(p[4]), p[5]);
    }
}
