package gymtracker.models;

/**
 * Abstract base class representing a person in the gym system.
 * Demonstrates: Abstract classes (inheritance base).
 */
public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;

    public Person(String id, String name, String email, String phone) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.phone = phone;
    }

    // ── Getters ──────────────────────────────────────────────
    public String getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    // ── Setters ──────────────────────────────────────────────
    public void setName(String name)   { this.name  = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Subclasses must declare their role (e.g. "Member", "Premium Member"). */
    public abstract String getRole();

    /** Subclasses must provide a one-line summary string. */
    public abstract String getSummary();

    @Override
    public String toString() {
        return String.format("[%s] %s (ID: %s)", getRole(), name, id);
    }
}
