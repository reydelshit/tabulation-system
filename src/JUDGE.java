
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author Reydel
 */
public class JUDGE extends javax.swing.JFrame {

    /**
     * Creates new form JUDGE
     */
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private final CANDIDATES_DETAILS candidatesDetails;

//    private final int id;
//    private final String name;
    private int id;
    private String name;

    // ...
    public void setId(int id) {
        this.id = id;
        JUDGE_ID_WELCOME.setText(String.valueOf(id));

    }

    public void setName(String name) {
        this.name = name;
        JUDGE_NAME_WELCOME.setText(name);

    }

    public JUDGE() {

        conn = DBConnection.getConnection();

        initComponents();

        RETRIEVE_CANDIDATE();
        DISPLAY_USED_CRITERIA();
        RETRIEVE_CANDIDATE_FOR_JUDGE();

        candidatesDetails = new CANDIDATES_DETAILS();
        candidatesDetails.setVisible(false);

        CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selectedBatch = CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE.getSelectedItem();
                if (selectedBatch.equals("All")) {
                    RETRIEVE_CANDIDATE();
                } else {
                    RETRIEVE_CANDIDATE_FOR_JUDGE();

                }
            }
        });

        JUDGE_USED_CRITERIA.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JTable table = (JTable) evt.getSource();
                int row = table.getSelectedRow();

                String title = table.getValueAt(row, 0).toString();
                int outOf = Integer.parseInt(table.getValueAt(row, 1).toString());

                CRITERIA_TITLE_LABEL.setText(title);
                CRITERIA_OUT_OF.setText(Integer.toString(outOf));

            }
        });

        LIST_OF_CANDIDATES_FOR_JUDGE.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {

                JTable table = (JTable) evt.getSource();
                int rowSelected = table.getSelectedRow();
                int columnSelected = table.getSelectedColumn();

                if (columnSelected <= 4) {
                    String title = table.getValueAt(rowSelected, 1).toString();
                    int id = Integer.parseInt(table.getValueAt(rowSelected, 0).toString());

                    CRITERIA_NAME.setText(title);
                    JUDGE_CANDIDATE_ID.setText(Integer.toString(id));
                }

                int column = LIST_OF_CANDIDATES_FOR_JUDGE.getColumnModel().getColumnIndexAtX(evt.getX());
                int row = evt.getY() / LIST_OF_CANDIDATES_FOR_JUDGE.getRowHeight();

                if (row < LIST_OF_CANDIDATES_FOR_JUDGE.getRowCount() && column == 5) {
                    int id = (int) LIST_OF_CANDIDATES_FOR_JUDGE.getValueAt(row, 0);
                    candidatesDetails.RETRIEVE_CANDIDATE(id);
                    candidatesDetails.setVisible(true);
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TABULATION = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        JUDGE_USED_CRITERIA = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        CRITERIA_JUDGE_SCORE = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        CRITERIA_OUT_OF = new javax.swing.JLabel();
        CRITERIA_TITLE_LABEL = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        CRITERIA_NAME = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        LIST_OF_CANDIDATES_FOR_JUDGE = new javax.swing.JTable();
        CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE = new javax.swing.JComboBox<>();
        JUDGE_NAME_WELCOME = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        JUDGE_ID_WELCOME = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        JUDGE_CANDIDATE_ID = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TABULATION.setBackground(new java.awt.Color(255, 153, 153));
        TABULATION.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        JUDGE_USED_CRITERIA.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(JUDGE_USED_CRITERIA);

        TABULATION.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 430, 680, 230));

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel3.add(CRITERIA_JUDGE_SCORE, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 380, 130, 40));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("OUT OF");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 80, 30));

        CRITERIA_OUT_OF.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        CRITERIA_OUT_OF.setText("number diria");
        jPanel3.add(CRITERIA_OUT_OF, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 300, 100, 30));

        CRITERIA_TITLE_LABEL.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        CRITERIA_TITLE_LABEL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CRITERIA_TITLE_LABEL.setText("CRITERIA diria");
        jPanel3.add(CRITERIA_TITLE_LABEL, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 380, 50));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Name");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 80, 30));

        jButton3.setText("SUBMIT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 500, 90, 40));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("CRITERIA");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 80, 30));

        CRITERIA_NAME.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        CRITERIA_NAME.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CRITERIA_NAME.setText("name diria");
        jPanel3.add(CRITERIA_NAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 380, 50));

        TABULATION.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 100, 420, 570));

        LIST_OF_CANDIDATES_FOR_JUDGE.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(LIST_OF_CANDIDATES_FOR_JUDGE);

        TABULATION.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 80, 680, 330));

        CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Teenager(Male)", "Teenager(Female)", "Kids(Male)", "Kids(Female)" }));
        TABULATION.add(CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 30, 140, 30));

        JUDGE_NAME_WELCOME.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        JUDGE_NAME_WELCOME.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        JUDGE_NAME_WELCOME.setText("Username");
        TABULATION.add(JUDGE_NAME_WELCOME, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, 140, 50));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Welcome ,");
        TABULATION.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 20, 140, 50));

        JUDGE_ID_WELCOME.setText("id diria");
        TABULATION.add(JUDGE_ID_WELCOME, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 30, 80, 40));

        jButton1.setText("Logout");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        TABULATION.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 660, -1, -1));

        JUDGE_CANDIDATE_ID.setText(" id sa candidate");
        TABULATION.add(JUDGE_CANDIDATE_ID, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 30, 80, 40));

        getContentPane().add(TABULATION, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1320, 699));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        LOGIN s = new LOGIN();

        s.setVisible(true);
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        SUBMIT_SCORE();
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JUDGE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JUDGE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JUDGE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JUDGE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JUDGE().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE;
    private javax.swing.JTextField CRITERIA_JUDGE_SCORE;
    private javax.swing.JLabel CRITERIA_NAME;
    private javax.swing.JLabel CRITERIA_OUT_OF;
    private javax.swing.JLabel CRITERIA_TITLE_LABEL;
    private javax.swing.JLabel JUDGE_CANDIDATE_ID;
    private javax.swing.JLabel JUDGE_ID_WELCOME;
    private javax.swing.JLabel JUDGE_NAME_WELCOME;
    private javax.swing.JTable JUDGE_USED_CRITERIA;
    private javax.swing.JTable LIST_OF_CANDIDATES_FOR_JUDGE;
    private javax.swing.JPanel TABULATION;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    private void DISPLAY_USED_CRITERIA() {
        try {
            String query = "SELECT title, outof FROM criteria WHERE isUsed = true";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            DefaultTableModel tableModel = new DefaultTableModel();

            tableModel.addColumn("Title");
            tableModel.addColumn("Out of");

            // Populate the table model with data from the result set
            while (rs.next()) {
                String title = rs.getString("title");
                String outof = rs.getString("outof");

                // Add a row to the table model
                tableModel.addRow(new Object[]{title, outof});
            }

            // Set the table model for the existing JTable component
            JUDGE_USED_CRITERIA.setModel(tableModel);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void RETRIEVE_CANDIDATE() {
        try {
            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Clear the existing data in the table
            tableModel.setRowCount(0);

            String query = "SELECT id, name, gender, candidate_no, category FROM candidate";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            // Define column names for the table
            String[] columnNames = {"id", "Name", "Gender", "Candidate No.", "Category", "Action"};

            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String gender = rs.getString("gender");
                int candidate_no = rs.getInt("candidate_no");
                String category = rs.getString("category");
                String pressMe = "View Details";

                tableModel.addRow(new Object[]{id, name, gender, candidate_no, category, pressMe});
            }

            LIST_OF_CANDIDATES_FOR_JUDGE.setCellSelectionEnabled(false);
            LIST_OF_CANDIDATES_FOR_JUDGE.setModel(tableModel);
            tableModel.fireTableDataChanged();
            // Refresh the table to update its content
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void RETRIEVE_CANDIDATE_FOR_JUDGE() {
        try {
            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Clear the existing data in the table
            tableModel.setRowCount(0);

            String selectedCategory = (String) CANDIDATE_CATEGORY_DROPDOWN_FORJUDGE.getSelectedItem();

            String query = "SELECT id, name, gender, candidate_no, category FROM candidate WHERE category = ? AND gender = ?";
            pst = conn.prepareStatement(query);
            if (selectedCategory.contains("Teenager") && selectedCategory.contains("Male")) {
                pst.setString(1, "Teenager");
                pst.setString(2, "Male");
            } else if (selectedCategory.contains("Teenager") && selectedCategory.contains("Female")) {
                pst.setString(1, "Teenager");
                pst.setString(2, "Female");
            } else if (selectedCategory.contains("Kids") && selectedCategory.contains("Male")) {
                pst.setString(1, "Kids");
                pst.setString(2, "Male");
            } else if (selectedCategory.contains("Kids") && selectedCategory.contains("Female")) {
                pst.setString(1, "Kids");
                pst.setString(2, "Female");
            } else {
                System.out.println("Invalid selected category: " + selectedCategory);
                return;
            }

            rs = pst.executeQuery();

            // Define column names for the table
            String[] columnNames = {"id", "Name", "Gender", "Candidate No.", "Category", "Action"};

            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String gender = rs.getString("gender");
                int candidate_no = rs.getInt("candidate_no");
                String category = rs.getString("category");
                String pressMe = "View Details";

                tableModel.addRow(new Object[]{id, name, gender, candidate_no, category, pressMe});
            }

            LIST_OF_CANDIDATES_FOR_JUDGE.setCellSelectionEnabled(false);
            LIST_OF_CANDIDATES_FOR_JUDGE.setModel(tableModel);

            // Refresh the table to update its content
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void SUBMIT_SCORE() {
        try {

            String sql = "INSERT INTO scores (criteria_title, outOf, judge_score, judge_id, candidate_id) VALUES (?, ?, ?, ?, ?)";

            pst = conn.prepareStatement(sql);
            pst.setString(1, CRITERIA_TITLE_LABEL.getText());
            pst.setString(2, CRITERIA_OUT_OF.getText());
            pst.setString(3, CRITERIA_JUDGE_SCORE.getText());
            pst.setString(4, JUDGE_ID_WELCOME.getText());
            pst.setString(5, JUDGE_CANDIDATE_ID.getText());


            pst.execute();

            JOptionPane.showMessageDialog(null, "Succesfully submitted the score");

            CRITERIA_TITLE_LABEL.setText("");
            CRITERIA_OUT_OF.setText("");
            CRITERIA_JUDGE_SCORE.setText("");
            JUDGE_CANDIDATE_ID.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            JOptionPane.showMessageDialog(null, "Error");

        }
    }

}
