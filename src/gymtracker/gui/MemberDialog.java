package gymtracker.gui;

import gymtracker.exceptions.MemberNotFoundException;
import gymtracker.models.Member;
import gymtracker.models.PremiumMember;
import gymtracker.services.MemberService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Modal dialog for adding or editing a member.
 * Demonstrates: Swing dialogs, form validation, exception handling in GUI.
 */
public class MemberDialog extends JDialog {

    private final MemberService memberService;
    private final Member        existingMember;   // null when adding
    private boolean             confirmed = false;
    private Member              resultMember;

    private JTextField   nameField, emailField, phoneField;
    private JTextField   trainerField, nutritionField;
    private JComboBox<Member.MembershipType> typeCombo;
    private JCheckBox    premiumCheck;
    private JPanel       premiumPanel;

    public MemberDialog(Frame parent, String title,
                        Member member, MemberService memberService) {
        super(parent, title, true);
        this.existingMember = member;
        this.memberService  = memberService;
        initUI();
        if (member != null) populateFields(member);
        pack();
        setMinimumSize(new Dimension(420, 0));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ── UI ────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 4, 4, 4);

        nameField      = new JTextField(22);
        emailField     = new JTextField(22);
        phoneField     = new JTextField(22);
        typeCombo      = new JComboBox<>(Member.MembershipType.values());
        premiumCheck   = new JCheckBox("Upgrade to Premium Member");
        trainerField   = new JTextField(22);
        nutritionField = new JTextField(22);

        int row = 0;
        addRow(form, g, row++, "Full Name:",   nameField);
        addRow(form, g, row++, "Email:",       emailField);
        addRow(form, g, row++, "Phone:",       phoneField);
        addRow(form, g, row++, "Membership:",  typeCombo);

        g.gridy  = row++;
        g.gridx  = 1;
        g.gridwidth = 1;
        form.add(premiumCheck, g);

        // Premium-only fields
        premiumPanel = new JPanel(new GridBagLayout());
        premiumPanel.setBorder(BorderFactory.createTitledBorder("Premium Details"));
        GridBagConstraints pg = new GridBagConstraints();
        pg.fill   = GridBagConstraints.HORIZONTAL;
        pg.insets = new Insets(3, 5, 3, 5);
        addRow(premiumPanel, pg, 0, "Personal Trainer:", trainerField);
        addRow(premiumPanel, pg, 1, "Nutrition Plan:",   nutritionField);
        premiumPanel.setVisible(false);

        g.gridy     = row++;
        g.gridx     = 0;
        g.gridwidth = 2;
        form.add(premiumPanel, g);

        premiumCheck.addActionListener(e -> {
            premiumPanel.setVisible(premiumCheck.isSelected());
            pack();
        });

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        saveBtn.setBackground(new Color(39, 174, 96));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setOpaque(true);
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> save());
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        add(form,   BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy     = row;
        g.gridx     = 0;
        g.gridwidth = 1;
        g.weightx   = 0;
        panel.add(new JLabel(label), g);
        g.gridx   = 1;
        g.weightx = 1.0;
        panel.add(field, g);
    }

    private void populateFields(Member m) {
        nameField.setText(m.getName());
        emailField.setText(m.getEmail());
        phoneField.setText(m.getPhone());
        typeCombo.setSelectedItem(m.getMembershipType());
        if (m instanceof PremiumMember) {
            PremiumMember pm = (PremiumMember) m;
            premiumCheck.setSelected(true);
            premiumPanel.setVisible(true);
            trainerField.setText(pm.getPersonalTrainer());
            nutritionField.setText(pm.getNutritionPlan());
        }
    }

    // ── Save logic with validation ────────────────────────────
    private void save() {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            warn("Name, email, and phone are required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            warn("Please enter a valid email address.");
            return;
        }

        Member.MembershipType type =
                (Member.MembershipType) typeCombo.getSelectedItem();

        String id       = existingMember != null ? existingMember.getId()
                                                 : memberService.generateId();
        String joinDate = existingMember != null ? existingMember.getJoinDate()
                                                 : LocalDate.now().toString();

        Member member;
        if (premiumCheck.isSelected()) {
            member = new PremiumMember(id, name, email, phone, type, joinDate,
                    trainerField.getText().trim(),
                    nutritionField.getText().trim());
        } else {
            member = new Member(id, name, email, phone, type, joinDate);
        }

        try {
            if (existingMember != null) {
                memberService.updateMember(member);
            } else {
                memberService.addMember(member);
            }
            resultMember = member;
            confirmed    = true;
            dispose();
        } catch (MemberNotFoundException ex) {
            error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Accessors ─────────────────────────────────────────────
    public boolean isConfirmed()       { return confirmed; }
    public Member  getResultMember()   { return resultMember; }
}
