package gymtracker.models;

/**
 * Premium gym member with a personal trainer and nutrition plan.
 * Demonstrates: Multi-level inheritance (PremiumMember → Member → Person).
 */
public class PremiumMember extends Member {

    private String personalTrainer;
    private String nutritionPlan;

    public PremiumMember(String id, String name, String email, String phone,
                         MembershipType membershipType, String joinDate,
                         String personalTrainer, String nutritionPlan) {
        super(id, name, email, phone, MembershipType.PREMIUM, joinDate);
        this.personalTrainer = personalTrainer;
        this.nutritionPlan   = nutritionPlan;
    }

    // ── Getters / setters ────────────────────────────────────
    public String getPersonalTrainer()              { return personalTrainer; }
    public String getNutritionPlan()                { return nutritionPlan; }
    public void setPersonalTrainer(String trainer)  { this.personalTrainer = trainer; }
    public void setNutritionPlan(String plan)       { this.nutritionPlan   = plan; }

    // ── Overrides ────────────────────────────────────────────
    @Override
    public String getRole() { return "Premium Member"; }

    @Override
    public String getSummary() {
        return super.getSummary()
                + String.format(" | Trainer: %-15s | Plan: %s", personalTrainer, nutritionPlan);
    }

    @Override
    public String getProgressReport() {
        return super.getProgressReport()
                + "\nPersonal Trainer : " + personalTrainer
                + "\nNutrition Plan   : " + nutritionPlan + "\n";
    }

    @Override
    public String exportToCSV() {
        return String.join(",",
                id, name, email, phone,
                membershipType.name(), joinDate,
                String.valueOf(monthlyFee),
                "true",
                personalTrainer, nutritionPlan);
    }

    /** Extra perks shown in the UI. */
    public String[] getPremiumBenefits() {
        return new String[]{
            "Unlimited gym access (24/7)",
            "Personal trainer sessions",
            "Customised nutrition planning",
            "Priority class booking",
            "Exclusive locker room access",
            "2 guest passes per month"
        };
    }
}
