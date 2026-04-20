package gymtracker.gui;

import gymtracker.exceptions.InvalidWorkoutException;
import gymtracker.exceptions.MemberNotFoundException;
import gymtracker.models.Member;
import gymtracker.models.WorkoutSession;
import gymtracker.services.MemberService;
import gymtracker.services.WorkoutService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Workouts tab — log new sessions and browse history.
 * Demonstrates: Exception handling in GUI, JComboBox, JSpinner, JSplitPane.
 */
public class WorkoutPanel extends JPanel {

    private final MemberService  memberService;
    private final WorkoutService workoutService;
    private final MainFrame      mainFrame;

    // Log-form widgets
    private JComboBox<String>  memberCombo;
    private JComboBox<String>  typeCombo;
    private JSpinner           durationSpinner;
    private JSpinner           caloriesSpinner;
    private JTextField         notesField;

    // History widgets
    private JTable            historyTable;
    private DefaultTableModel historyModel;
    private JComboBox<String> filterCombo;
    private boolean           refreshing = false;

    private static final String[] WORKOUT_TYPES = {
        "Running", "Cycling", "Swimming", "Weight Training",
        "Yoga", "Pilates", "HIIT", "Cardio", "CrossFit", "Boxing", "Other"
    };

    private static final String[] HISTORY_COLS = {
        "Session ID", "Member", "Date", "Type", "Duration (min)", "Calories", "Notes"
    };

    public WorkoutPanel(MemberService memberService,
                        WorkoutService workoutService,
                        MainFrame mainFrame) {
        this.memberService  = memberService;
        this.workoutService = workoutService;
        this.mainFrame      = mainFrame;
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLogPanel(), buildHistoryPanel());
        split.setDividerLocation(300);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);
    }

    // ── Log panel (left) ──────────────────────────────────────
    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                "Log New Workout",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(41, 128, 185)));
        panel.setPreferredSize(new Dimension(295, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.fill      = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 2;
        g.insets    = new Insets(4, 8, 2, 8);

        memberCombo    = new JComboBox<>();
        typeCombo      = new JComboBox<>(WORKOUT_TYPES);
        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 500, 5));
        caloriesSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 5000, 10));
        notesField     = new JTextField();

        int row = 0;
        row = addFormField(panel, g, row, "Member:",            memberCombo);
        row = addFormField(panel, g, row, "Workout Type:",      typeCombo);
        row = addFormField(panel, g, row, "Duration (min):",    durationSpinner);
        row = addFormField(panel, g, row, "Calories Burned:",   caloriesSpinner);
        row = addFormField(panel, g, row, "Notes (optional):",  notesField);

        JButton logBtn = new JButton("  Log Workout  ");
        logBtn.setBackground(new Color(39, 174, 96));
        logBtn.setForeground(Color.WHITE);
        logBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logBtn.setFocusPainted(false);
        logBtn.setBorderPainted(false);
        logBtn.setOpaque(true);

        g.gridy  = row;
        g.insets = new Insets(16, 8, 8, 8);
        panel.add(logBtn, g);

        logBtn.addActionListener(e -> logWorkout());
        return panel;
    }

    private int addFormField(JPanel p, GridBagConstraints g, int row,
                             String label, JComponent field) {
        g.gridy  = row;
        g.insets = new Insets(6, 8, 1, 8);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lbl, g);

        g.gridy  = row + 1;
        g.insets = new Insets(1, 8, 4, 8);
        p.add(field, g);
        return row + 2;
    }

    // ── History panel (right) ─────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Workout History"));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterCombo = new JComboBox<>();
        filterCombo.addItem("All Members");
        filterBar.add(new JLabel("Filter by member:"));
        filterBar.add(filterCombo);
        filterCombo.addActionListener(e -> { if (!refreshing) filterHistory(); });

        // Table
        historyModel = new DefaultTableModel(HISTORY_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(24);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setGridColor(new Color(220, 220, 220));

        int[] widths = {80, 130, 100, 120, 100, 80, 200};
        for (int i = 0; i < widths.length; i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        panel.add(filterBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Refresh ───────────────────────────────────────────────
    public void refresh() {
        refreshing = true;
        try {
            String prevMember = (String) memberCombo.getSelectedItem();
            String prevFilter = (String) filterCombo.getSelectedItem();

            memberCombo.removeAllItems();
            filterCombo.removeAllItems();
            filterCombo.addItem("All Members");

            for (Member m : memberService.getAllMembers()) {
                String entry = m.getId() + " - " + m.getName();
                memberCombo.addItem(entry);
                filterCombo.addItem(entry);
            }

            if (prevMember != null) memberCombo.setSelectedItem(prevMember);
            if (prevFilter != null) filterCombo.setSelectedItem(prevFilter);
        } finally {
            refreshing = false;
        }
        filterHistory();
    }

    private void filterHistory() {
        String sel = (String) filterCombo.getSelectedItem();
        if (sel == null || sel.equals("All Members")) {
            showHistory(workoutService.getAllWorkouts());
        } else {
            String memberId = sel.split(" - ")[0];
            showHistory(workoutService.getWorkoutsForMember(memberId));
        }
    }

    private void showHistory(List<WorkoutSession> sessions) {
        historyModel.setRowCount(0);
        for (int i = sessions.size() - 1; i >= 0; i--) {   // newest first
            WorkoutSession s = sessions.get(i);
            String memberName;
            try {
                memberName = memberService.getMember(s.getMemberId()).getName();
            } catch (MemberNotFoundException e) {
                memberName = s.getMemberId();
            }
            historyModel.addRow(new Object[]{
                s.getSessionId(), memberName, s.getDate(), s.getWorkoutType(),
                s.getDurationMinutes(), s.getCaloriesBurned(), s.getNotes()
            });
        }
    }

    // ── Log workout action ────────────────────────────────────
    private void logWorkout() {
        String entry = (String) memberCombo.getSelectedItem();
        if (entry == null) {
            JOptionPane.showMessageDialog(this,
                    "Please register a member first.", "No Members",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String memberId    = entry.split(" - ")[0];
        String memberName  = entry.contains(" - ") ? entry.split(" - ", 2)[1] : entry;
        String workoutType = (String) typeCombo.getSelectedItem();
        int    duration    = (int) durationSpinner.getValue();
        int    calories    = (int) caloriesSpinner.getValue();
        String notes       = notesField.getText().trim();

        WorkoutSession session = new WorkoutSession(memberId, workoutType, duration, calories, notes);

        try {
            workoutService.logWorkout(session);
            notesField.setText("");
            refresh();
            mainFrame.setStatus("Logged: " + workoutType + " for " + memberName);
            JOptionPane.showMessageDialog(this,
                    "Workout logged!\n\n"
                    + "Type     : " + workoutType + "\n"
                    + "Duration : " + duration + " min\n"
                    + "Calories : " + calories + " cal\n"
                    + "Member   : " + memberName,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidWorkoutException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Workout", JOptionPane.ERROR_MESSAGE);
        } catch (MemberNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
