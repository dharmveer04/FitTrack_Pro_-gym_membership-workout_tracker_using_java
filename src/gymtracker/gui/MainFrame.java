package gymtracker.gui;

import gymtracker.filehandling.DataManager;
import gymtracker.models.Member;
import gymtracker.models.WorkoutSession;
import gymtracker.services.MemberService;
import gymtracker.services.WorkoutService;
import gymtracker.threads.AutoSaveThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

/**
 * Application main window.
 * Demonstrates: Swing JFrame, JTabbedPane, integration of all layers.
 */
public class MainFrame extends JFrame {

    private final DataManager    dataManager;
    private final MemberService  memberService;
    private final WorkoutService workoutService;
    private final AutoSaveThread autoSaveThread;

    private DashboardPanel dashboardPanel;
    private MembersPanel   membersPanel;
    private WorkoutPanel   workoutPanel;
    private JLabel         statusLabel;

    public MainFrame() {
        dataManager    = new DataManager();
        memberService  = new MemberService();
        workoutService = new WorkoutService(memberService);

        loadData();

        // ── Auto-save thread (multithreading demo) ────────────
        autoSaveThread = new AutoSaveThread(dataManager, memberService, workoutService, 30);
        autoSaveThread.setOnSaveCallback(() ->
                SwingUtilities.invokeLater(() ->
                        setStatus("Auto-saved at " + java.time.LocalTime.now().toString().substring(0, 8))));
        autoSaveThread.start();

        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                autoSaveThread.save();      // final save before exit
                autoSaveThread.stopSaving();
                dispose();
                System.exit(0);
            }
        });
    }

    // ── Persistence ───────────────────────────────────────────
    private void loadData() {
        try {
            List<Member>         members  = dataManager.loadMembers();
            memberService.setMembers(members);
            List<WorkoutSession> sessions = dataManager.loadWorkouts();
            workoutService.setAllSessions(sessions);
            System.out.println("[App] Loaded " + members.size() + " member(s) and "
                    + sessions.size() + " workout(s).");
        } catch (IOException e) {
            System.err.println("[App] Could not load saved data: " + e.getMessage());
        }
    }

    // ── UI construction ───────────────────────────────────────
    private void initUI() {
        setTitle("FitTrack Pro — Gym Membership & Workout Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 720);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        // ── Header bar ────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 47, 61));
        header.setPreferredSize(new Dimension(0, 58));

        JLabel appTitle = new JLabel("  FitTrack Pro", JLabel.LEFT);
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appTitle.setForeground(Color.WHITE);

        JLabel appSub = new JLabel("Gym Membership & Workout Tracker  ", JLabel.RIGHT);
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appSub.setForeground(new Color(174, 182, 191));

        header.add(appTitle, BorderLayout.WEST);
        header.add(appSub,   BorderLayout.EAST);

        // ── Tabbed pane ───────────────────────────────────────
        dashboardPanel = new DashboardPanel(memberService, workoutService);
        membersPanel   = new MembersPanel(memberService, workoutService, this);
        workoutPanel   = new WorkoutPanel(memberService, workoutService, this);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("  Dashboard  ",  dashboardPanel);
        tabs.addTab("  Members  ",    membersPanel);
        tabs.addTab("  Workouts  ",   workoutPanel);
        tabs.addChangeListener(e -> {
            switch (tabs.getSelectedIndex()) {
                case 0 -> dashboardPanel.refresh();
                case 1 -> membersPanel.refresh();
                case 2 -> workoutPanel.refresh();
            }
        });

        // ── Status bar ────────────────────────────────────────
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 24));

        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel autoSaveLbl = new JLabel("Auto-save: every 30 s  ");
        autoSaveLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        autoSaveLbl.setForeground(new Color(100, 100, 100));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(autoSaveLbl, BorderLayout.EAST);

        // ── Layout ────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(header,    BorderLayout.NORTH);
        add(tabs,      BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── Public helpers ────────────────────────────────────────
    public void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText("  " + msg);
    }

    public void refreshAll() {
        dashboardPanel.refresh();
        membersPanel.refresh();
        workoutPanel.refresh();
    }

    public MemberService  getMemberService()  { return memberService; }
    public WorkoutService getWorkoutService() { return workoutService; }
}
