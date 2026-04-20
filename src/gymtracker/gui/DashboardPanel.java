package gymtracker.gui;

import gymtracker.models.Member;
import gymtracker.models.WorkoutSession;
import gymtracker.services.MemberService;
import gymtracker.services.WorkoutService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Dashboard tab — shows live statistics and recent activity.
 */
public class DashboardPanel extends JPanel {

    private final MemberService  memberService;
    private final WorkoutService workoutService;

    private JLabel totalMembersLabel;
    private JLabel premiumMembersLabel;
    private JLabel todayWorkoutsLabel;
    private JLabel totalCaloriesLabel;
    private JTextArea recentArea;
    private JTextArea topMembersArea;

    public DashboardPanel(MemberService memberService, WorkoutService workoutService) {
        this.memberService  = memberService;
        this.workoutService = workoutService;
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(236, 240, 241));

        // ── Stat cards ────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setBackground(new Color(236, 240, 241));
        statsRow.setPreferredSize(new Dimension(0, 110));

        totalMembersLabel   = new JLabel("0", JLabel.CENTER);
        premiumMembersLabel = new JLabel("0", JLabel.CENTER);
        todayWorkoutsLabel  = new JLabel("0", JLabel.CENTER);
        totalCaloriesLabel  = new JLabel("0", JLabel.CENTER);

        statsRow.add(makeCard("Total Members",      totalMembersLabel,   new Color(41, 128, 185)));
        statsRow.add(makeCard("Premium Members",    premiumMembersLabel, new Color(142, 68, 173)));
        statsRow.add(makeCard("Today's Workouts",   todayWorkoutsLabel,  new Color(39, 174,  96)));
        statsRow.add(makeCard("Total Calories Burned", totalCaloriesLabel, new Color(211, 84,  0)));

        // ── Lower panels ──────────────────────────────────────
        JPanel lower = new JPanel(new GridLayout(1, 2, 10, 0));
        lower.setBackground(new Color(236, 240, 241));

        recentArea = new JTextArea();
        recentArea.setEditable(false);
        recentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        recentArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane recentScroll = new JScrollPane(recentArea);
        recentScroll.setBorder(titledBorder("Recent Workouts (last 10)"));

        topMembersArea = new JTextArea();
        topMembersArea.setEditable(false);
        topMembersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        topMembersArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane topScroll = new JScrollPane(topMembersArea);
        topScroll.setBorder(titledBorder("Top Members by Workouts"));

        lower.add(recentScroll);
        lower.add(topScroll);

        add(statsRow, BorderLayout.NORTH);
        add(lower,    BorderLayout.CENTER);
    }

    private JPanel makeCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titleLbl = new JLabel(title, JLabel.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(255, 255, 255, 200));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12));
    }

    /** Refresh all statistics from the service layer. */
    public void refresh() {
        totalMembersLabel.setText(String.valueOf(memberService.getTotalMembers()));
        premiumMembersLabel.setText(String.valueOf(memberService.getPremiumMemberCount()));
        todayWorkoutsLabel.setText(String.valueOf(workoutService.getTodayWorkouts().size()));
        totalCaloriesLabel.setText(String.format("%,d", workoutService.getTotalCaloriesBurned()));

        // Recent workouts (newest first)
        List<WorkoutSession> all = workoutService.getAllWorkouts();
        StringBuilder recent = new StringBuilder();
        for (int i = all.size() - 1; i >= Math.max(0, all.size() - 10); i--) {
            WorkoutSession s = all.get(i);
            recent.append(String.format("%-12s %-16s %3d min  %4d cal%n",
                    s.getDate(), s.getWorkoutType(),
                    s.getDurationMinutes(), s.getCaloriesBurned()));
        }
        recentArea.setText(recent.length() > 0 ? recent.toString() : "(no workouts logged yet)");

        // Top members
        StringBuilder top = new StringBuilder();
        memberService.getAllMembers().stream()
                .sorted((a, b) -> b.getTotalWorkouts() - a.getTotalWorkouts())
                .limit(10)
                .forEach(m -> top.append(String.format("%-22s %3d workouts  %,d cal%n",
                        m.getName(), m.getTotalWorkouts(), m.getTotalCaloriesBurned())));
        topMembersArea.setText(top.length() > 0 ? top.toString() : "(no members registered yet)");
    }
}
