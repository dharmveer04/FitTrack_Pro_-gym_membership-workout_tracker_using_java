package gymtracker.exceptions;

/**
 * Thrown when a member lookup fails.
 */
public class MemberNotFoundException extends Exception {
    private final String memberId;

    public MemberNotFoundException(String memberId) {
        super("Member not found with ID: " + memberId);
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }
}
