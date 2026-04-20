package gymtracker.services;

import gymtracker.exceptions.MemberNotFoundException;
import gymtracker.models.Member;
import gymtracker.models.PremiumMember;
import gymtracker.models.WorkoutSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business-logic layer for member management.
 * Demonstrates: Custom exception handling, service pattern.
 */
public class MemberService {

    private List<Member> members;
    private static int idCounter = 1000;

    public MemberService() {
        this.members = new ArrayList<>();
    }

    /** Generate a unique member ID like MEM1001. */
    public String generateId() {
        return "MEM" + (++idCounter);
    }

    /**
     * Add a new member.
     * @throws IllegalArgumentException if the email is already registered.
     */
    public void addMember(Member member) {
        boolean duplicate = members.stream()
                .anyMatch(m -> m.getEmail().equalsIgnoreCase(member.getEmail()));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "A member with email '" + member.getEmail() + "' already exists.");
        }
        members.add(member);
    }

    /**
     * Look up a member by ID.
     * @throws MemberNotFoundException if no member has that ID.
     */
    public Member getMember(String id) throws MemberNotFoundException {
        return members.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    /**
     * Remove a member by ID.
     * @throws MemberNotFoundException if the ID does not exist.
     */
    public void removeMember(String id) throws MemberNotFoundException {
        Member m = getMember(id);
        members.remove(m);
    }

    /**
     * Replace an existing member with an updated version,
     * preserving the original workout-session list.
     */
    public void updateMember(Member updated) throws MemberNotFoundException {
        Member existing = getMember(updated.getId());
        // Carry over workout history so it isn't lost on edit
        for (WorkoutSession s : existing.getWorkoutSessions()) {
            updated.addWorkoutSession(s);
        }
        int idx = members.indexOf(existing);
        members.set(idx, updated);
    }

    public List<Member> getAllMembers() {
        return Collections.unmodifiableList(members);
    }

    /** Case-insensitive search across name, ID and email. */
    public List<Member> searchMembers(String query) {
        String q = query.toLowerCase();
        return members.stream()
                .filter(m -> m.getName().toLowerCase().contains(q)
                          || m.getId().toLowerCase().contains(q)
                          || m.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Member> getActiveMembers() {
        return members.stream().filter(Member::isActive).collect(Collectors.toList());
    }

    public int getTotalMembers()      { return members.size(); }

    public int getPremiumMemberCount() {
        return (int) members.stream().filter(m -> m instanceof PremiumMember).count();
    }

    /** Bulk-load from persistence layer; also synchronises the ID counter. */
    public void setMembers(List<Member> loaded) {
        this.members = new ArrayList<>(loaded);
        loaded.stream()
              .map(Member::getId)
              .filter(id -> id.startsWith("MEM"))
              .mapToInt(id -> {
                  try { return Integer.parseInt(id.substring(3)); }
                  catch (NumberFormatException e) { return 0; }
              })
              .max()
              .ifPresent(max -> idCounter = Math.max(idCounter, max));
    }
}
