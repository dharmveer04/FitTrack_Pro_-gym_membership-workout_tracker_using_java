package gymtracker.gui;

import gymtracker.exceptions.MemberNotFoundException;
import gymtracker.models.Member;
import gymtracker.services.MemberService;
import gymtracker.services.WorkoutService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Members tab — CRUD operations with a searchable JTable.
 */
public class MembersPanel extends JPanel {

    private final MemberService  memberService;
    private final WorkoutService workoutService;
    private final MainFrame      mainFrame;

    private JTable             memberTable;
    private DefaultTableModel  tableModel;
    private JTextField         searchField;

    private static final String[] COLS = {
        "ID", "Name", "Email", "Phone", "Type", "Join Date", "Monthly Fee", "Status"
    };

    public MembersPanel(MemberService memberService,
                        WorkoutService workoutService,
                        MainFrame mainFrame) {
        this.memberService  = memberService;
        this.workoutService = workoutService;
        this.mainFrame      = mainFrame;
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Toolbar ───────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

        JButton addBtn    = styledBtn("+ Add",       new Color(39, 174, 96));
        JButton editBtn   = styledBtn("Edit",        new Color(41, 128, 185));
        JButton deleteBtn = styledBtn("Delete",      new Color(192, 57, 43));
        JButton viewBtn   = styledBtn("Progress",    new Color(142, 68, 173));
        JButton benefitsBtn = styledBtn("Benefits",  new Color(211, 84, 0));

        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton clearBtn  = new JButton("Clear");

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(viewBtn);
        toolbar.add(benefitsBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(clearBtn);

        // ── Table ─────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        memberTable = new JTable(tableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setRowHeight(26);
        memberTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        memberTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        memberTable.setGridColor(new Color(220, 220, 220));

        int[] widths = {80, 150, 200, 120, 110, 100, 100, 80};
        for (int i = 0; i < widths.length; i++) {
            memberTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // ── Listeners ─────────────────────────────────────────
        addBtn.addActionListener(e -> doAdd());
        editBtn.addActionListener(e -> doEdit());
        deleteBtn.addActionListener(e -> doDelete());
        viewBtn.addActionListener(e -> doViewProgress());
        benefitsBtn.addActionListener(e -> doShowBenefits());
        searchBtn.addActionListener(e -> doSearch());
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchField.addActionListener(e -> doSearch());

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(memberTable), BorderLayout.CENTER);

        // row count label
        JLabel hint = new JLabel("Double-click a row to edit");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        add(hint, BorderLayout.SOUTH);

        memberTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doEdit();
            }
        });
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }

    // ── Refresh ───────────────────────────────────────────────
    public void refresh() {
        populate(memberService.getAllMembers());
    }

    private void populate(List<Member> list) {
        tableModel.setRowCount(0);
        for (Member m : list) {
            tableModel.addRow(new Object[]{
                m.getId(), m.getName(), m.getEmail(), m.getPhone(),
                m.getRole(), m.getJoinDate(),
                String.format("$%.2f", m.getMonthlyFee()),
                m.isActive() ? "Active" : "Inactive"
            });
        }
        mainFrame.setStatus("Showing " + list.size() + " member(s).");
    }

    // ── Helpers ───────────────────────────────────────────────
    private String selectedId() {
        int row = memberTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a member first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    // ── Actions ───────────────────────────────────────────────
    private void doAdd() {
        MemberDialog dlg = new MemberDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Member", null, memberService);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            refresh();
            mainFrame.setStatus("Member added: " + dlg.getResultMember().getName());
        }
    }

    private void doEdit() {
        String id = selectedId();
        if (id == null) return;
        try {
            Member m = memberService.getMember(id);
            MemberDialog dlg = new MemberDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Member", m, memberService);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                refresh();
                mainFrame.setStatus("Member updated: " + dlg.getResultMember().getName());
            }
        } catch (MemberNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        String id = selectedId();
        if (id == null) return;
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete member " + id + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                memberService.removeMember(id);
                refresh();
                mainFrame.setStatus("Member " + id + " deleted.");
            } catch (MemberNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doViewProgress() {
        String id = selectedId();
        if (id == null) return;
        try {
            Member m = memberService.getMember(id);
            JTextArea area = new JTextArea(m.getProgressReport(), 20, 50);
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area),
                    "Progress Report — " + m.getName(), JOptionPane.INFORMATION_MESSAGE);
        } catch (MemberNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doShowBenefits() {
        String id = selectedId();
        if (id == null) return;
        try {
            Member m = memberService.getMember(id);
            if (m instanceof gymtracker.models.PremiumMember) {
                gymtracker.models.PremiumMember pm = (gymtracker.models.PremiumMember) m;
                StringBuilder sb = new StringBuilder("Premium Benefits for " + pm.getName() + ":\n\n");
                for (String b : pm.getPremiumBenefits()) sb.append("  ✓ ").append(b).append("\n");
                JOptionPane.showMessageDialog(this, sb.toString(),
                        "Premium Benefits", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        m.getName() + " is not a Premium member.\n"
                        + "Edit their profile to upgrade!",
                        "Not Premium", JOptionPane.WARNING_MESSAGE);
            }
        } catch (MemberNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { refresh(); return; }
        List<Member> results = memberService.searchMembers(q);
        populate(results);
        mainFrame.setStatus("Search \"" + q + "\" — " + results.size() + " result(s).");
    }
}
