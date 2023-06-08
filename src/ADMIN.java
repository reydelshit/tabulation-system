
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author Reydel
 */
public class ADMIN extends javax.swing.JFrame {

    /**
     * Creates new form ADMIN
     */
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    CardLayout cardLayout;

    private final CANDIDATES_DETAILS candidatesDetails;

    public ADMIN() {
        conn = DBConnection.getConnection();
        initComponents();

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(CANDIDATE_MALE);
        buttonGroup.add(CANDIDATE_FEMALE);

        candidatesDetails = new CANDIDATES_DETAILS();
        candidatesDetails.setVisible(false);

        RETRIEVE_CANDIDATE();
        DISPLAY_ACCOUNT_JUDGE();
        DISPLAY_ACCOUNT_JUDGE();
        DISPLAY_CRITERIA();
        DISPLAY_USED_CRITERIA();
        RETRIEVE_CANDIDATE_FORTABULATION();
        TABULATION_FETCH_BASED_ON_CRITERIA();

        CRITERIA_WINNER();
        OVERALL_WINNER_BYCATEGORY();

        CANDIDATE_SELECTED_GENDER.setVisible(false);

        cardLayout = (CardLayout) (PAGES.getLayout());

        LIST_OF_CANDIDATES_TABLE.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int column = LIST_OF_CANDIDATES_TABLE.getColumnModel().getColumnIndexAtX(evt.getX());
                int row = evt.getY() / LIST_OF_CANDIDATES_TABLE.getRowHeight();

                if (row < LIST_OF_CANDIDATES_TABLE.getRowCount() && column == 3) {
                    int id = (int) LIST_OF_CANDIDATES_TABLE.getValueAt(row, 0);
                    candidatesDetails.RETRIEVE_CANDIDATE(id);
                    candidatesDetails.setVisible(true);
//                    System.out.println("dniashbd");
                }
            }
        });

        CANDIDATE_CATEGORY_DROPDOWN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selectedBatch = CANDIDATE_CATEGORY_DROPDOWN.getSelectedItem();
                if (selectedBatch.equals("All")) {
                    RETRIEVE_CANDIDATE();
                } else {
                    RETRIEVE_CANDIDATE_BYCATEGORY();

                }
            }
        });

        CANDIDATE_CATEGORY_DROPDOWN_TABULATION.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selectedBatch = CANDIDATE_CATEGORY_DROPDOWN_TABULATION.getSelectedItem();
                if (selectedBatch.equals("All")) {
                    RETRIEVE_CANDIDATE_FORTABULATION();
                } else {
                    RETRIEVE_CANDIDATE_FORTABULATION_BYCATEGORY();

                }
            }
        });

        TABULATION_LIST_OF_CANDIDATES.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JTable table = (JTable) evt.getSource();
                int row = table.getSelectedRow();
                int id = Integer.parseInt(table.getValueAt(row, 0).toString());
                TABULATION_CANDIDATE_ID.setText(String.valueOf(id));

                try {

                    String query = "SELECT s.criteria_title, c.name AS candidate_name, s.outOf, s.judge_score, s.judge_id, j.fullname AS judge_name "
                            + "FROM scores s "
                            + "JOIN candidate c ON s.candidate_id = c.id "
                            + "JOIN judge j ON s.judge_id = j.judge_id"
                            + " WHERE c.id = ?";

                    pst = conn.prepareStatement(query);
                    pst.setInt(1, id);
                    rs = pst.executeQuery();

                    DefaultTableModel tableModel = new DefaultTableModel();
                    tableModel.addColumn("Criteria Title");
                    tableModel.addColumn("Candidate Name");
                    tableModel.addColumn("Out Of (Max Score)");
                    tableModel.addColumn("Judge's Score");
                    tableModel.addColumn("Judge Name");

                    Map<String, Double> criterionTotalScores = new HashMap<>();
                    Map<String, Double> judgeScores = new HashMap<>();

                    double overallTotalScore = 0.0;
                    int judgeCount = 0;

                    while (rs.next()) {
                        String criteriaTitle = rs.getString("criteria_title");
                        String candidateName = rs.getString("candidate_name");
                        int outOf = rs.getInt("outOf");
                        double judgeScore = rs.getDouble("judge_score");
                        String judgeName = rs.getString("judge_name");

                        double judgeScorePercentage = (judgeScore / outOf) * 100;

                        // Update the total score for the current criterion
                        if (criterionTotalScores.containsKey(criteriaTitle)) {
                            double currentTotalScore = criterionTotalScores.get(criteriaTitle);
                            currentTotalScore += judgeScorePercentage;
                            criterionTotalScores.put(criteriaTitle, currentTotalScore);
                        } else {
                            criterionTotalScores.put(criteriaTitle, judgeScorePercentage);
                        }

                        // Update the sum of scores for the current judge
                        if (judgeScores.containsKey(judgeName)) {
                            double currentJudgeScore = judgeScores.get(judgeName);
                            currentJudgeScore += judgeScorePercentage;
                            judgeScores.put(judgeName, currentJudgeScore);
                        } else {
                            judgeScores.put(judgeName, judgeScorePercentage);
                            judgeCount++;
                        }

                        tableModel.addRow(new Object[]{criteriaTitle, candidateName, outOf, judgeScore, judgeName});
                    }

                    TABULATION_TABLE.setModel(tableModel);

                    // Set the layout manager of TABULATION_SCORES
                    TABULATION_SCORES_CONTAINER.setLayout(new BoxLayout(TABULATION_SCORES_CONTAINER, BoxLayout.Y_AXIS));

                    TABULATION_SCORES_CONTAINER.removeAll();

                    DecimalFormat decimalFormat = new DecimalFormat("0.0");

                    for (Map.Entry<String, Double> entry : criterionTotalScores.entrySet()) {
                        String criteriaTitle = entry.getKey();
                        double totalScore = entry.getValue() / judgeCount;

                        overallTotalScore += totalScore;

                        JLabel criterionLabel = new JLabel(criteriaTitle);
                        JLabel scoreLabel = new JLabel(decimalFormat.format(totalScore));

                        TABULATION_SCORES_CONTAINER.add(Box.createVerticalStrut(10));

                        TABULATION_SCORES_CONTAINER.add(criterionLabel);
                        TABULATION_SCORES_CONTAINER.add(scoreLabel);
                    }

                    TABULATION_TOTAL_SCORES.setText(String.valueOf(decimalFormat.format(overallTotalScore / judgeCount))); // Calculate the average overall score
                    TABULATION_OUT_OF.setText(String.valueOf(100));

                    TABULATION_SCORES_CONTAINER.revalidate();
                    TABULATION_SCORES_CONTAINER.repaint();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e);
                }
            }
        });

        CANDIDATE_CATEGORY_DROPDOWN_WINNERS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedCategory = (String) CANDIDATE_CATEGORY_DROPDOWN_WINNERS.getSelectedItem();
                OVERALL_WINNER_BYCATEGORY();
                CRITERIA_WINNER();

                if (selectedCategory.contains("Teenager") && selectedCategory.contains("Male")) {
                    WINNER_CATEGORY_TITLE.setText("TEENAGER (MALE)");
                } else if (selectedCategory.contains("Teenager") && selectedCategory.contains("Female")) {
                    WINNER_CATEGORY_TITLE.setText("TEENAGER (FEMALE)");
                } else if (selectedCategory.contains("Kids") && selectedCategory.contains("Male")) {
                    WINNER_CATEGORY_TITLE.setText("KIDS (MALE)");
                } else if (selectedCategory.contains("Kids") && selectedCategory.contains("Female")) {
                    WINNER_CATEGORY_TITLE.setText("KIDS (FEMALE)");
                } else {
                    System.out.println("Invalid selected category: " + selectedCategory);
                    return;
                }
            }
        });
    }

    private byte[] selectedImageData;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        PAGES = new javax.swing.JPanel();
        MAIN_PANEL = new javax.swing.JPanel();
        CANDITATES = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        UPLOAD_BUTTON = new javax.swing.JButton();
        CANDIDATE_IMAGE_LABEL = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        CANDIDATE_NAME = new javax.swing.JTextField();
        CANDIDATE_AGE = new javax.swing.JTextField();
        CANDIDATE_BDATE = new com.toedter.calendar.JDateChooser();
        CANDIDATE_MALE = new javax.swing.JRadioButton();
        CANDIDATE_FEMALE = new javax.swing.JRadioButton();
        CANDIDATE_SELECTED_GENDER = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        CANDIDATE_CATEGORY_DROPDOWN = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        LIST_OF_CANDIDATES_TABLE = new javax.swing.JTable();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        CANDIDATE_FOR_UPDATE_DECIDER = new javax.swing.JLabel();
        CANDIDATE_FOR_UPDATE_ID = new javax.swing.JLabel();
        JUDGES = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        CREATE_FULLNAME = new javax.swing.JTextField();
        CREATE_USERNAME = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        CREATE_WARNING = new javax.swing.JLabel();
        CREATE_PASSWORD = new javax.swing.JPasswordField();
        CREATE_REPASSWORD = new javax.swing.JPasswordField();
        jLabel13 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        JUDGE_TABLE_ACCOUNTS = new javax.swing.JTable();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        JUDGE_UPDATE_DECIDER = new javax.swing.JLabel();
        JUDGE_UPDATE_ID = new javax.swing.JLabel();
        CRITERIA = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        CRITERIA_OUTOF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        CRITERIA_TITLE = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        CRITERIA_ID_FORUPDATE = new javax.swing.JLabel();
        CRITERIA_UPDATE_DECIDER = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        CRITERIA_TABLE = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        CRITERIA_USED = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        TABULATION = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TABULATION_TABLE = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        TABULATION_LIST_OF_CANDIDATES = new javax.swing.JTable();
        CANDIDATE_CATEGORY_DROPDOWN_TABULATION = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        TABULATION_SCORES_CONTAINER = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        TABULATION_TOTAL_SCORES = new javax.swing.JLabel();
        TABULATION_OUT_OF = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        TABULATION_CRITERIA_TITLE_DROPDOWN = new javax.swing.JComboBox<>();
        TABULATION_CANDIDATE_ID = new javax.swing.JLabel();
        WINNERS = new javax.swing.JPanel();
        CANDIDATE_CATEGORY_DROPDOWN_WINNERS = new javax.swing.JComboBox<>();
        jScrollPane7 = new javax.swing.JScrollPane();
        NGEKOY = new javax.swing.JTable();
        WINNER_CATEGORY = new javax.swing.JLabel();
        WINNER_CATEGORY_SCORES = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        WINNER_CATEGORY_TITLE = new javax.swing.JLabel();
        WINNER_IMAGE_WINNER = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("JUDGES");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 200, 41));

        jButton2.setText("CANDIDATES");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 200, 41));

        jButton3.setText("MANAGE CRITERIA");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 200, 41));

        jLabel1.setText("DASDADA");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 590, 40, -1));

        jButton7.setText("TABULATION");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 200, 41));

        jButton12.setText("Logout");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 640, -1, -1));

        jButton13.setText("WINNERS");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 390, 200, 41));

        jSplitPane1.setLeftComponent(jPanel1);

        PAGES.setBackground(new java.awt.Color(204, 255, 204));
        PAGES.setLayout(new java.awt.CardLayout());

        MAIN_PANEL.setBackground(new java.awt.Color(255, 153, 255));

        javax.swing.GroupLayout MAIN_PANELLayout = new javax.swing.GroupLayout(MAIN_PANEL);
        MAIN_PANEL.setLayout(MAIN_PANELLayout);
        MAIN_PANELLayout.setHorizontalGroup(
            MAIN_PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        MAIN_PANELLayout.setVerticalGroup(
            MAIN_PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        PAGES.add(MAIN_PANEL, "card4");

        CANDITATES.setBackground(new java.awt.Color(102, 102, 0));
        CANDITATES.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton4.setText("ADD CANDIDATE");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 550, 160, 40));

        UPLOAD_BUTTON.setText("UPLOAD");
        UPLOAD_BUTTON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UPLOAD_BUTTONActionPerformed(evt);
            }
        });
        jPanel2.add(UPLOAD_BUTTON, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 100, 110, 30));

        CANDIDATE_IMAGE_LABEL.setText("IMG DIRIA HA");
        jPanel2.add(CANDIDATE_IMAGE_LABEL, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 220, 210));

        jPanel4.setBackground(new java.awt.Color(204, 204, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel4.add(CANDIDATE_NAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 170, 40));
        jPanel4.add(CANDIDATE_AGE, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 170, 40));
        jPanel4.add(CANDIDATE_BDATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 170, 40));

        CANDIDATE_MALE.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        CANDIDATE_MALE.setText("MALE");
        CANDIDATE_MALE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CANDIDATE_MALEActionPerformed(evt);
            }
        });
        jPanel4.add(CANDIDATE_MALE, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 40, -1, -1));

        CANDIDATE_FEMALE.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        CANDIDATE_FEMALE.setText("FEMALE");
        CANDIDATE_FEMALE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CANDIDATE_FEMALEActionPerformed(evt);
            }
        });
        jPanel4.add(CANDIDATE_FEMALE, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, -1, -1));

        CANDIDATE_SELECTED_GENDER.setText("yes");
        jPanel4.add(CANDIDATE_SELECTED_GENDER, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 60, 40, 30));

        jLabel3.setText("Fullname");
        jPanel4.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 60, -1));

        jLabel4.setText("Age");
        jPanel4.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 60, -1));

        jLabel5.setText("Birthday");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 60, -1));

        jLabel15.setText("Gender");
        jPanel4.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 20, 60, -1));

        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 390, 260));

        CANDITATES.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 430, 610));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("LIST OF CANDIDATES");
        CANDITATES.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 30, 150, 40));

        CANDIDATE_CATEGORY_DROPDOWN.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Teenager(Male)", "Teenager(Female)", "Kids(Male)", "Kids(Female)" }));
        CANDITATES.add(CANDIDATE_CATEGORY_DROPDOWN, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 80, 140, 30));

        LIST_OF_CANDIDATES_TABLE.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Candidate No", "Button"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(LIST_OF_CANDIDATES_TABLE);
        if (LIST_OF_CANDIDATES_TABLE.getColumnModel().getColumnCount() > 0) {
            LIST_OF_CANDIDATES_TABLE.getColumnModel().getColumn(0).setResizable(false);
            LIST_OF_CANDIDATES_TABLE.getColumnModel().getColumn(1).setResizable(false);
            LIST_OF_CANDIDATES_TABLE.getColumnModel().getColumn(2).setResizable(false);
        }

        CANDITATES.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 120, -1, 490));

        jButton16.setText("Delete");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });
        CANDITATES.add(jButton16, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 630, 100, 30));

        jButton17.setText("Update");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        CANDITATES.add(jButton17, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 630, 100, 30));

        CANDIDATE_FOR_UPDATE_DECIDER.setText("update decider");
        CANDITATES.add(CANDIDATE_FOR_UPDATE_DECIDER, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 90, -1, -1));

        CANDIDATE_FOR_UPDATE_ID.setText("id update");
        CANDITATES.add(CANDIDATE_FOR_UPDATE_ID, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 90, -1, -1));

        PAGES.add(CANDITATES, "PAGE_1");

        JUDGES.setBackground(new java.awt.Color(204, 255, 255));
        JUDGES.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(26, 46, 53));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 217, 90));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("CREATE JUDGE ACCOUNT");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, 290, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 217, 90));
        jLabel8.setText("FULL NAME");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 100, 80, 20));

        jLabel10.setText("CLECK");
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel10MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jLabel10MouseReleased(evt);
            }
        });
        jPanel3.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 230, 50, 40));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 217, 90));
        jLabel11.setText("ENTER USERNAME");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, 110, 20));
        jPanel3.add(CREATE_FULLNAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 120, 230, 30));
        jPanel3.add(CREATE_USERNAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 180, 230, 30));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 217, 90));
        jLabel12.setText("ENTER PASSWORD");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 220, 110, 20));

        CREATE_WARNING.setForeground(new java.awt.Color(255, 217, 90));
        CREATE_WARNING.setText(" ");
        jPanel3.add(CREATE_WARNING, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 340, 140, 20));
        jPanel3.add(CREATE_PASSWORD, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 242, 230, 30));

        CREATE_REPASSWORD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                CREATE_REPASSWORDKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                CREATE_REPASSWORDKeyTyped(evt);
            }
        });
        jPanel3.add(CREATE_REPASSWORD, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 300, 230, 30));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 217, 90));
        jLabel13.setText("RE-ENTER PASSWORD");
        jPanel3.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 280, 130, 20));

        jButton5.setText("CREATE");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 380, 170, 40));

        JUDGES.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, 400, 530));

        JUDGE_TABLE_ACCOUNTS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(JUDGE_TABLE_ACCOUNTS);

        JUDGES.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 90, 520, 440));

        jButton14.setText("Delete");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });
        JUDGES.add(jButton14, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 540, 100, 30));

        jButton15.setText("Update");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        JUDGES.add(jButton15, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 540, 100, 30));

        JUDGE_UPDATE_DECIDER.setText("update decider");
        JUDGES.add(JUDGE_UPDATE_DECIDER, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 50, 100, -1));

        JUDGE_UPDATE_ID.setText("id update");
        JUDGES.add(JUDGE_UPDATE_ID, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 50, -1, -1));

        PAGES.add(JUDGES, "PAGE_2");

        CRITERIA.setBackground(new java.awt.Color(102, 102, 0));
        CRITERIA.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel5.setBackground(new java.awt.Color(204, 255, 255));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel5.add(CRITERIA_OUTOF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 120, 70, 41));

        jLabel9.setText("OUT OF NO. (ex. /10)");
        jPanel5.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, 130, -1));

        jLabel14.setText("CRITERIA TITLE");
        jPanel5.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 100, -1));
        jPanel5.add(CRITERIA_TITLE, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 253, 41));

        jButton6.setText("Submit");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 180, 220, 40));

        CRITERIA_ID_FORUPDATE.setText("id update");
        jPanel5.add(CRITERIA_ID_FORUPDATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 10, -1, -1));

        CRITERIA_UPDATE_DECIDER.setText("no");
        jPanel5.add(CRITERIA_UPDATE_DECIDER, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 10, -1, -1));

        CRITERIA.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, 330, 250));

        CRITERIA_TABLE.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "Tittle", "Out of"
            }
        ));
        jScrollPane2.setViewportView(CRITERIA_TABLE);
        if (CRITERIA_TABLE.getColumnModel().getColumnCount() > 0) {
            CRITERIA_TABLE.getColumnModel().getColumn(0).setMaxWidth(20);
        }

        CRITERIA.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 490, 510));

        CRITERIA_USED.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Title", "Out Of"
            }
        ));
        jScrollPane4.setViewportView(CRITERIA_USED);

        CRITERIA.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 320, 330, 310));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("CURRENTLY USED CRITERIA FOR JUDGING");
        CRITERIA.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 290, 300, 30));

        jButton8.setText("Delete");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        CRITERIA.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 60, 100, 30));

        jButton9.setText("Remove");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        CRITERIA.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 640, 100, 30));

        jButton10.setText("Update");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        CRITERIA.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 20, 100, 30));

        jButton11.setText("Use");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        CRITERIA.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 540, 100, 30));

        PAGES.add(CRITERIA, "CRITERIA");

        TABULATION.setBackground(new java.awt.Color(255, 153, 153));
        TABULATION.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TABULATION_TABLE.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(TABULATION_TABLE);

        TABULATION.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, 690, 410));

        TABULATION_LIST_OF_CANDIDATES.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane6.setViewportView(TABULATION_LIST_OF_CANDIDATES);

        TABULATION.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 300, 410));

        CANDIDATE_CATEGORY_DROPDOWN_TABULATION.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Teenager(Male)", "Teenager(Female)", "Kids(Male)", "Kids(Female)" }));
        TABULATION.add(CANDIDATE_CATEGORY_DROPDOWN_TABULATION, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, 140, 30));

        jPanel6.setBackground(new java.awt.Color(255, 204, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TABULATION_SCORES_CONTAINER.setBackground(new java.awt.Color(255, 204, 255));

        javax.swing.GroupLayout TABULATION_SCORES_CONTAINERLayout = new javax.swing.GroupLayout(TABULATION_SCORES_CONTAINER);
        TABULATION_SCORES_CONTAINER.setLayout(TABULATION_SCORES_CONTAINERLayout);
        TABULATION_SCORES_CONTAINERLayout.setHorizontalGroup(
            TABULATION_SCORES_CONTAINERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        TABULATION_SCORES_CONTAINERLayout.setVerticalGroup(
            TABULATION_SCORES_CONTAINERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        jPanel6.add(TABULATION_SCORES_CONTAINER, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 160, 150));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("/");
        jPanel6.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 166, 10, 20));

        TABULATION_TOTAL_SCORES.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TABULATION_TOTAL_SCORES.setText("0.0");
        jPanel6.add(TABULATION_TOTAL_SCORES, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 160, 40, 30));

        TABULATION_OUT_OF.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TABULATION_OUT_OF.setText("0.0");
        jPanel6.add(TABULATION_OUT_OF, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 160, 40, 30));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("TOTAL SCORES:");
        jPanel6.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 90, -1));

        TABULATION.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 490, 280, 200));

        TABULATION_CRITERIA_TITLE_DROPDOWN.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Criteria (All)" }));
        TABULATION.add(TABULATION_CRITERIA_TITLE_DROPDOWN, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 22, 280, 30));

        TABULATION_CANDIDATE_ID.setText("candidate_id");
        TABULATION.add(TABULATION_CANDIDATE_ID, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 30, 80, -1));

        PAGES.add(TABULATION, "TABULATION");

        WINNERS.setBackground(new java.awt.Color(0, 102, 102));
        WINNERS.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        CANDIDATE_CATEGORY_DROPDOWN_WINNERS.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Teenager(Male)", "Teenager(Female)", "Kids(Male)", "Kids(Female)" }));
        WINNERS.add(CANDIDATE_CATEGORY_DROPDOWN_WINNERS, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 70, 140, 30));

        NGEKOY.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane7.setViewportView(NGEKOY);

        WINNERS.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 340, -1, 180));

        WINNER_CATEGORY.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        WINNER_CATEGORY.setForeground(new java.awt.Color(255, 255, 255));
        WINNER_CATEGORY.setText("NAMEE");
        WINNERS.add(WINNER_CATEGORY, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 140, 280, 40));

        WINNER_CATEGORY_SCORES.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        WINNER_CATEGORY_SCORES.setForeground(new java.awt.Color(255, 255, 255));
        WINNER_CATEGORY_SCORES.setText("WINNERS BASED ON CRITERIA");
        WINNERS.add(WINNER_CATEGORY_SCORES, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 220, 280, 40));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("WINNERS BASED ON CRITERIA");
        WINNERS.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 290, 280, 40));

        WINNER_CATEGORY_TITLE.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        WINNER_CATEGORY_TITLE.setForeground(new java.awt.Color(255, 255, 255));
        WINNER_CATEGORY_TITLE.setText("NAMEE");
        WINNERS.add(WINNER_CATEGORY_TITLE, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, 280, 40));

        WINNER_IMAGE_WINNER.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        WINNER_IMAGE_WINNER.setForeground(new java.awt.Color(255, 255, 255));
        WINNER_IMAGE_WINNER.setText("IMAGE HERE");
        WINNERS.add(WINNER_IMAGE_WINNER, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 150, 450, 490));

        PAGES.add(WINNERS, "WINNERS");

        jSplitPane1.setRightComponent(PAGES);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 699, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        cardLayout.show(PAGES, "PAGE_1");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cardLayout.show(PAGES, "PAGE_2");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void UPLOAD_BUTTONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UPLOAD_BUTTONActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Get the selected image file
                java.io.File file = fileChooser.getSelectedFile();

                // Read the image file and convert it into a byte array
                FileInputStream fis = new FileInputStream(file);
                byte[] imageData = new byte[(int) file.length()];
                fis.read(imageData);
                fis.close();

                selectedImageData = imageData;

                // Display the selected image
                ImageIcon imageIcon = new ImageIcon(imageData);
                int labelWidth = CANDIDATE_IMAGE_LABEL.getWidth();
                int labelHeight = CANDIDATE_IMAGE_LABEL.getHeight();
                Image originalImage = imageIcon.getImage();
                int originalWidth = originalImage.getWidth(null);
                int originalHeight = originalImage.getHeight(null);

                // Calculate the scaling factors for width and height
                double widthScaleFactor = (double) labelWidth / originalWidth;
                double heightScaleFactor = (double) labelHeight / originalHeight;

                // Use the higher scaling factor to maintain aspect ratio and choose the minimum scale factor
                double scale = Math.min(widthScaleFactor, heightScaleFactor) * 2;

                // Calculate the scaled dimensions
                int scaledWidth = (int) (originalWidth * scale);
                int scaledHeight = (int) (originalHeight * scale);

                // Create a scaled instance of the image
                Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                // Update the label with the scaled image
                CANDIDATE_IMAGE_LABEL.setIcon(new ImageIcon(scaledImage));
                // System.out.println("Image selected and displayed successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }


    }//GEN-LAST:event_UPLOAD_BUTTONActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        ADD_CANDIDATES();
        RETRIEVE_CANDIDATE();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void CANDIDATE_FEMALEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CANDIDATE_FEMALEActionPerformed
        CANDIDATE_SELECTED_GENDER.setText("Female");
    }//GEN-LAST:event_CANDIDATE_FEMALEActionPerformed

    private void CANDIDATE_MALEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CANDIDATE_MALEActionPerformed
        CANDIDATE_SELECTED_GENDER.setText("Male");
    }//GEN-LAST:event_CANDIDATE_MALEActionPerformed

    private void jLabel10MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MousePressed
        CREATE_PASSWORD.setEchoChar((char) 0);
    }//GEN-LAST:event_jLabel10MousePressed

    private void jLabel10MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseReleased
        CREATE_PASSWORD.setEchoChar('*');
    }//GEN-LAST:event_jLabel10MouseReleased

    private void CREATE_REPASSWORDKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CREATE_REPASSWORDKeyReleased
        checkPasswordMatch();
    }//GEN-LAST:event_CREATE_REPASSWORDKeyReleased

    private void CREATE_REPASSWORDKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CREATE_REPASSWORDKeyTyped

    }//GEN-LAST:event_CREATE_REPASSWORDKeyTyped

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        CREATE_ACCOUNT();
        DISPLAY_ACCOUNT_JUDGE();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        ADD_CRITERIA();
        DISPLAY_CRITERIA();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        cardLayout.show(PAGES, "CRITERIA");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        cardLayout.show(PAGES, "TABULATION");
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        int selectedRow = CRITERIA_USED.getSelectedRow();

        if (selectedRow != -1) {
            int id = Integer.parseInt(CRITERIA_USED.getValueAt(selectedRow, 0).toString());

            try {
                String query = "UPDATE criteria SET isUsed = ? WHERE criteria_id = ?";
                pst = conn.prepareStatement(query);
                pst.setBoolean(1, false);
                pst.setInt(2, id);

                pst.executeUpdate();

                DISPLAY_CRITERIA();
                DISPLAY_USED_CRITERIA();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a row from the table.");
        }

    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        int selectedRow = CRITERIA_TABLE.getSelectedRow();

        if (selectedRow != -1) {
            int id = Integer.parseInt(CRITERIA_TABLE.getValueAt(selectedRow, 0).toString());

            try {
                String query = "UPDATE criteria SET isUsed = ? WHERE criteria_id = ?";
                pst = conn.prepareStatement(query);
                pst.setBoolean(1, true);
                pst.setInt(2, id);

                pst.executeUpdate();

                DISPLAY_CRITERIA();
                DISPLAY_USED_CRITERIA();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a row from the table.");
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        LOGIN s = new LOGIN();

        s.setVisible(true);
        setVisible(false);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        cardLayout.show(PAGES, "WINNERS");
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        int selectedRow = CRITERIA_TABLE.getSelectedRow();
        String id = CRITERIA_TABLE.getValueAt(selectedRow, 0).toString();

        try {
            String query = "DELETE FROM criteria WHERE criteria_id = ?";
            pst = conn.prepareStatement(query);
            pst.setString(1, id);

            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Criteria deleted successfully!");
                DISPLAY_CRITERIA();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delte criteria.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        int selectedRow = CRITERIA_TABLE.getSelectedRow();
        String id = CRITERIA_TABLE.getValueAt(selectedRow, 0).toString();
        String title = CRITERIA_TABLE.getValueAt(selectedRow, 1).toString();
        String outOf = CRITERIA_TABLE.getValueAt(selectedRow, 2).toString();

        CRITERIA_ID_FORUPDATE.setText(String.valueOf(id));
        CRITERIA_UPDATE_DECIDER.setText("update");

        CRITERIA_TITLE.setText(title);
        CRITERIA_OUTOF.setText(outOf);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        int selectedRow = JUDGE_TABLE_ACCOUNTS.getSelectedRow();
        String id = JUDGE_TABLE_ACCOUNTS.getValueAt(selectedRow, 0).toString();

        try {
            String query = "DELETE FROM judge WHERE judge_id = ?";
            pst = conn.prepareStatement(query);
            pst.setString(1, id);

            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Judge deleted successfully!");
                DISPLAY_ACCOUNT_JUDGE();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete judge.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        int selectedRow = JUDGE_TABLE_ACCOUNTS.getSelectedRow();
        String id = JUDGE_TABLE_ACCOUNTS.getValueAt(selectedRow, 0).toString();
        String title = JUDGE_TABLE_ACCOUNTS.getValueAt(selectedRow, 1).toString();
        String username = JUDGE_TABLE_ACCOUNTS.getValueAt(selectedRow, 2).toString();

        JUDGE_UPDATE_ID.setText(String.valueOf(id));
        JUDGE_UPDATE_DECIDER.setText("update");

        CREATE_FULLNAME.setText(title);
        CREATE_USERNAME.setText(username);


    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        int selectedRow = LIST_OF_CANDIDATES_TABLE.getSelectedRow();
        String id = LIST_OF_CANDIDATES_TABLE.getValueAt(selectedRow, 0).toString();

        try {
            String query = "DELETE FROM candidate WHERE id = ?";
            pst = conn.prepareStatement(query);
            pst.setString(1, id);

            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Candidate deleted successfully!");
                RETRIEVE_CANDIDATE();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete candidate.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        int selectedRow = LIST_OF_CANDIDATES_TABLE.getSelectedRow();
        String id = LIST_OF_CANDIDATES_TABLE.getValueAt(selectedRow, 0).toString();
        String title = LIST_OF_CANDIDATES_TABLE.getValueAt(selectedRow, 1).toString();
        String username = LIST_OF_CANDIDATES_TABLE.getValueAt(selectedRow, 2).toString();
//        String gender = LIST_OF_CANDIDATES_TABLE.getValueAt(selectedRow, 2).toString();

        CANDIDATE_FOR_UPDATE_ID.setText(String.valueOf(id));
        CANDIDATE_FOR_UPDATE_DECIDER.setText("update");

        CREATE_FULLNAME.setText(title);
        CREATE_USERNAME.setText(username);

        try {
            String query = "SELECT name, age, gender, birthday, category, image FROM candidate WHERE id = ?";
            pst = conn.prepareStatement(query);
            pst.setInt(1, Integer.parseInt(id));
            rs = pst.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                Date birthday = rs.getDate("birthday");
                byte[] imageData = rs.getBytes("image");
                String gender = rs.getString("gender");

                if (gender.equalsIgnoreCase("Male")) {
                    CANDIDATE_MALE.setSelected(true);
                } else if (gender.equalsIgnoreCase("Female")) {
                    CANDIDATE_FEMALE.setSelected(true);
                }

                CANDIDATE_NAME.setText(name);
                CANDIDATE_AGE.setText(String.valueOf(age));
                CANDIDATE_BDATE.setDate(birthday);

//                    RETRIEVE IMAGE 
                ImageIcon imageIcon = new ImageIcon(imageData);
                Image scaledImage = imageIcon.getImage().getScaledInstance(CANDIDATE_IMAGE_LABEL.getWidth(), CANDIDATE_IMAGE_LABEL.getHeight(), Image.SCALE_SMOOTH);
                CANDIDATE_IMAGE_LABEL.setIcon(new ImageIcon(scaledImage));
                CANDIDATE_IMAGE_LABEL.setIcon(new ImageIcon(scaledImage));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }


    }//GEN-LAST:event_jButton17ActionPerformed

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
            java.util.logging.Logger.getLogger(ADMIN.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ADMIN.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ADMIN.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ADMIN.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ADMIN().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CANDIDATE_AGE;
    private com.toedter.calendar.JDateChooser CANDIDATE_BDATE;
    private javax.swing.JComboBox<String> CANDIDATE_CATEGORY_DROPDOWN;
    private javax.swing.JComboBox<String> CANDIDATE_CATEGORY_DROPDOWN_TABULATION;
    private javax.swing.JComboBox<String> CANDIDATE_CATEGORY_DROPDOWN_WINNERS;
    private javax.swing.JRadioButton CANDIDATE_FEMALE;
    private javax.swing.JLabel CANDIDATE_FOR_UPDATE_DECIDER;
    private javax.swing.JLabel CANDIDATE_FOR_UPDATE_ID;
    private javax.swing.JLabel CANDIDATE_IMAGE_LABEL;
    private javax.swing.JRadioButton CANDIDATE_MALE;
    private javax.swing.JTextField CANDIDATE_NAME;
    private javax.swing.JLabel CANDIDATE_SELECTED_GENDER;
    private javax.swing.JPanel CANDITATES;
    private javax.swing.JTextField CREATE_FULLNAME;
    private javax.swing.JPasswordField CREATE_PASSWORD;
    private javax.swing.JPasswordField CREATE_REPASSWORD;
    private javax.swing.JTextField CREATE_USERNAME;
    private javax.swing.JLabel CREATE_WARNING;
    private javax.swing.JPanel CRITERIA;
    private javax.swing.JLabel CRITERIA_ID_FORUPDATE;
    private javax.swing.JTextField CRITERIA_OUTOF;
    private javax.swing.JTable CRITERIA_TABLE;
    private javax.swing.JTextField CRITERIA_TITLE;
    private javax.swing.JLabel CRITERIA_UPDATE_DECIDER;
    private javax.swing.JTable CRITERIA_USED;
    private javax.swing.JPanel JUDGES;
    private javax.swing.JTable JUDGE_TABLE_ACCOUNTS;
    private javax.swing.JLabel JUDGE_UPDATE_DECIDER;
    private javax.swing.JLabel JUDGE_UPDATE_ID;
    private javax.swing.JTable LIST_OF_CANDIDATES_TABLE;
    private javax.swing.JPanel MAIN_PANEL;
    private javax.swing.JTable NGEKOY;
    private javax.swing.JPanel PAGES;
    private javax.swing.JPanel TABULATION;
    private javax.swing.JLabel TABULATION_CANDIDATE_ID;
    private javax.swing.JComboBox<String> TABULATION_CRITERIA_TITLE_DROPDOWN;
    private javax.swing.JTable TABULATION_LIST_OF_CANDIDATES;
    private javax.swing.JLabel TABULATION_OUT_OF;
    private javax.swing.JPanel TABULATION_SCORES_CONTAINER;
    private javax.swing.JTable TABULATION_TABLE;
    private javax.swing.JLabel TABULATION_TOTAL_SCORES;
    private javax.swing.JButton UPLOAD_BUTTON;
    private javax.swing.JPanel WINNERS;
    private javax.swing.JLabel WINNER_CATEGORY;
    private javax.swing.JLabel WINNER_CATEGORY_SCORES;
    private javax.swing.JLabel WINNER_CATEGORY_TITLE;
    private javax.swing.JLabel WINNER_IMAGE_WINNER;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

    private void ADD_CANDIDATES() {

        try {
            String sql;
            if (CANDIDATE_FOR_UPDATE_DECIDER.getText().equals("update")) {
                Date selectedDate = CANDIDATE_BDATE.getDate();
                String dateString = new java.sql.Date(selectedDate.getTime()).toString();

                sql = "UPDATE candidate SET name = ?, birthday = ?, age = ?, gender = ?, category = ?, candidate_no = ? WHERE id = ?";

                pst = conn.prepareStatement(sql);
                pst.setString(1, CANDIDATE_NAME.getText());
                pst.setString(2, dateString);
                pst.setInt(3, Integer.parseInt(CANDIDATE_AGE.getText()));
                pst.setString(4, CANDIDATE_SELECTED_GENDER.getText());

                int storeAge = Integer.parseInt(CANDIDATE_AGE.getText());
                int candidateNo;

                if (storeAge > 13) {
                    pst.setString(5, "Teenager");
                    candidateNo = GETCANDIDATE_NUMBER("Teenager", CANDIDATE_SELECTED_GENDER.getText());
                } else {
                    pst.setString(5, "Kids");
                    candidateNo = GETCANDIDATE_NUMBER("Kids", CANDIDATE_SELECTED_GENDER.getText());
                }

                pst.setInt(6, candidateNo);

                pst.setString(7, CANDIDATE_FOR_UPDATE_ID.getText());

                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "Successfully updated the candidate");

                CANDIDATE_NAME.setText("");
                CANDIDATE_AGE.setText("");
                CANDIDATE_SELECTED_GENDER.setText("");
                selectedImageData = null;
                CANDIDATE_IMAGE_LABEL.setIcon(null);
            } else {
                Date selectedDate = CANDIDATE_BDATE.getDate();
                String dateString = new java.sql.Date(selectedDate.getTime()).toString();

                sql = "INSERT INTO candidate (name, birthday, age, gender, category, candidate_no, image) VALUES (?, ?, ?, ?, ?, ?, ?)";

                pst = conn.prepareStatement(sql);
                pst.setString(1, CANDIDATE_NAME.getText());
                pst.setString(2, dateString);
                pst.setInt(3, Integer.parseInt(CANDIDATE_AGE.getText()));
                pst.setString(4, CANDIDATE_SELECTED_GENDER.getText());

                int storeAge = Integer.parseInt(CANDIDATE_AGE.getText());
                int candidateNo;

                if (storeAge > 13) {
                    pst.setString(5, "Teenager");
                    candidateNo = GETCANDIDATE_NUMBER("Teenager", CANDIDATE_SELECTED_GENDER.getText());
                } else {
                    pst.setString(5, "Kids");
                    candidateNo = GETCANDIDATE_NUMBER("Kids", CANDIDATE_SELECTED_GENDER.getText());
                }

                pst.setInt(6, candidateNo);
                pst.setBytes(7, selectedImageData);

                pst.execute();

                JOptionPane.showMessageDialog(null, "Candidate is added");

                CANDIDATE_NAME.setText("");
                CANDIDATE_AGE.setText("");
                CANDIDATE_SELECTED_GENDER.setText("");
                selectedImageData = null;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private int GETCANDIDATE_NUMBER(String category, String gender) {
        int candidateNo = 0;

        try {
            String query = "SELECT MAX(candidate_no) FROM candidate WHERE category = ? AND gender = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, category);
            pstmt.setString(2, gender);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                candidateNo = rs.getInt(1);
            }

            candidateNo++;

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

        return candidateNo;
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

            String query = "SELECT id, name, candidate_no FROM candidate";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            // Define column names for the table
            String[] columnNames = {"id", "Name", "Candidate No.", "Action"};

            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int candidateNo = rs.getInt("candidate_no");

                String pressMe = "View Details";
                // Add a row to the table model
                tableModel.addRow(new Object[]{id, name, candidateNo, pressMe});
            }

//            LIST_OF_CANDIDATES_TABLE.setCellSelectionEnabled(false);
            LIST_OF_CANDIDATES_TABLE.setModel(tableModel);
            tableModel.fireTableDataChanged();
            // Refresh the table to update its content
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void RETRIEVE_CANDIDATE_BYCATEGORY() {
        try {
            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Clear the existing data in the table
            tableModel.setRowCount(0);

            String selectedCategory = (String) CANDIDATE_CATEGORY_DROPDOWN.getSelectedItem();

            String query = "SELECT id, name, candidate_no FROM candidate WHERE category = ? AND gender = ?";
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
                System.out.println("Invalid selected category:" + selectedCategory);
            }

            rs = pst.executeQuery();

            // Define column names for the table
            String[] columnNames = {"id", "Name", "Candidate No.", "Action"};

            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int candidateNo = rs.getInt("candidate_no");

                String pressMe = "View Details";
                // Add a row to the table model
                tableModel.addRow(new Object[]{id, name, candidateNo, pressMe});
            }

            LIST_OF_CANDIDATES_TABLE.setCellSelectionEnabled(false);
            LIST_OF_CANDIDATES_TABLE.setModel(tableModel);
            // Refresh the table to update its content
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void checkPasswordMatch() {
        if (!Arrays.equals(CREATE_REPASSWORD.getPassword(), CREATE_PASSWORD.getPassword())) {
            CREATE_WARNING.setText("Passwords do not match");
        } else {
            CREATE_WARNING.setText("Passwords match");
        }
    }

    private void CREATE_ACCOUNT() {
        try {

            String sql;
            if (JUDGE_UPDATE_DECIDER.getText().equals("update")) {
                sql = "UPDATE judge SET fullname = ?, username = ?, password = ?, account_type = ? WHERE judge_id = ?";

                pst = conn.prepareStatement(sql);
                pst.setString(1, CREATE_FULLNAME.getText());
                pst.setString(2, CREATE_USERNAME.getText());
                String password = new String(CREATE_REPASSWORD.getPassword());
                pst.setString(3, password);
                pst.setString(4, "Judge");

                pst.setInt(5, Integer.parseInt(JUDGE_UPDATE_ID.getText()));

                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "Successfully updated the judge account");

                CREATE_FULLNAME.setText("");
                CREATE_USERNAME.setText("");
                CREATE_REPASSWORD.setText("");
                CREATE_PASSWORD.setText("");
            } else {
                sql = "INSERT INTO judge (fullname, username, password, account_type) VALUES (?, ?, ?, ?)";

                pst = conn.prepareStatement(sql);
                pst.setString(1, CREATE_FULLNAME.getText());
                pst.setString(2, CREATE_USERNAME.getText());
                String password = new String(CREATE_REPASSWORD.getPassword());
                pst.setString(3, password);
                pst.setString(4, "Judge");

                pst.execute();

                JOptionPane.showMessageDialog(null, "Succesfully created a judge account");

                CREATE_FULLNAME.setText("");
                CREATE_USERNAME.setText("");
                CREATE_REPASSWORD.setText("");
                CREATE_PASSWORD.setText("");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            JOptionPane.showMessageDialog(null, "Error");

        }
    }

    private void DISPLAY_ACCOUNT_JUDGE() {
        try {
            String query = "SELECT judge_id, fullname, username, account_type FROM judge";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            // Create the table model with column names
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("id");

            tableModel.addColumn("Full Name");
            tableModel.addColumn("Username");
            tableModel.addColumn("Account Type");

            // Populate the table model with data from the result set
            while (rs.next()) {
                int id = rs.getInt("judge_id");
                String fullName = rs.getString("fullname");
                String username = rs.getString("username");
                String accountType = rs.getString("account_type");

                // Add a row to the table model
                tableModel.addRow(new Object[]{id, fullName, username, accountType});
            }

            // Set the table model for the existing JTable component
            JUDGE_TABLE_ACCOUNTS.setModel(tableModel);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void ADD_CRITERIA() {
        try {
            String sql;
            if (CRITERIA_UPDATE_DECIDER.getText().equals("update")) {
                sql = "UPDATE criteria SET title = ?, outof = ?, isUsed = ? WHERE criteria_id = ?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, CRITERIA_TITLE.getText());
                pst.setInt(2, Integer.parseInt(CRITERIA_OUTOF.getText()));
                pst.setBoolean(3, false);
                pst.setInt(4, Integer.parseInt(CRITERIA_ID_FORUPDATE.getText()));

                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "Succesfully updated the criteria");
                CRITERIA_TITLE.setText("");
                CRITERIA_OUTOF.setText("");
                CRITERIA_ID_FORUPDATE.setText("");
                CRITERIA_UPDATE_DECIDER.setText("");

            } else {
                sql = "INSERT INTO criteria (title, outof, isUsed) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, CRITERIA_TITLE.getText());
                pst.setInt(2, Integer.parseInt(CRITERIA_OUTOF.getText()));
                pst.setBoolean(3, false);

                pst.execute();

                JOptionPane.showMessageDialog(null, "Succesfully added the criteria");

                CRITERIA_TITLE.setText("");
                CRITERIA_OUTOF.setText("");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            JOptionPane.showMessageDialog(null, "Error");

        }
    }

    private void DISPLAY_CRITERIA() {
        try {
            String query = "SELECT criteria_id, title, outof FROM criteria WHERE isUsed = false";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            DefaultTableModel tableModel = new DefaultTableModel();

            tableModel.addColumn("id");
            tableModel.addColumn("Title");
            tableModel.addColumn("Out of");

            while (rs.next()) {
                int id = rs.getInt("criteria_id");

                String title = rs.getString("title");
                String outof = rs.getString("outof");

                // Add a row to the table model
                tableModel.addRow(new Object[]{id, title, outof});
            }

            CRITERIA_TABLE.setModel(tableModel);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void DISPLAY_USED_CRITERIA() {
        try {
            String query = "SELECT criteria_id, title, outof FROM criteria WHERE isUsed = true";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("id");

            tableModel.addColumn("Title");
            tableModel.addColumn("Out of");

            // Populate the table model with data from the result set
            while (rs.next()) {
                int id = rs.getInt("criteria_id");
                String title = rs.getString("title");
                String outof = rs.getString("outof");

                // Add a row to the table model
                tableModel.addRow(new Object[]{id, title, outof});
            }

            // Set the table model for the existing JTable component
            CRITERIA_USED.setModel(tableModel);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void RETRIEVE_CANDIDATE_FORTABULATION() {
        try {
            DefaultTableModel tableModel = new DefaultTableModel();

            // Clear the existing data in the table
            tableModel.setRowCount(0);

            String query = "SELECT id, name, candidate_no FROM candidate";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            // Define column names for the table
            String[] columnNames = {"id", "Name", "Candidate No."};

            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                int id = rs.getInt("id");

                String name = rs.getString("name");
                String candidate_no = rs.getString("candidate_no");

                tableModel.addRow(new Object[]{id, name, candidate_no});
            }

            TABULATION_LIST_OF_CANDIDATES.setModel(tableModel);

            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void RETRIEVE_CANDIDATE_FORTABULATION_BYCATEGORY() {
        try {
            DefaultTableModel tableModel = new DefaultTableModel();

            // Clear the existing data in the table
            tableModel.setRowCount(0);
            String selectedCategory = (String) CANDIDATE_CATEGORY_DROPDOWN_TABULATION.getSelectedItem();

            String query = "SELECT id, name, candidate_no FROM candidate WHERE category = ? AND gender = ?";
            pst = conn.prepareStatement(query);

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
            String[] columnNames = {"id", "Name", "Candidate No."};

            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String candidate_no = rs.getString("candidate_no");

                tableModel.addRow(new Object[]{id, name, candidate_no});
            }

            TABULATION_LIST_OF_CANDIDATES.setModel(tableModel);

            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void TABULATION_FETCH_BASED_ON_CRITERIA() {

        try {

            String sql = "SELECT DISTINCT title FROM criteria";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                String criteria_title = rs.getString("title");
                TABULATION_CRITERIA_TITLE_DROPDOWN.addItem(criteria_title);
            }

            TABULATION_CRITERIA_TITLE_DROPDOWN.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selectedCriteria = (String) TABULATION_CRITERIA_TITLE_DROPDOWN.getSelectedItem();
                    // Clear the table
                    DefaultTableModel tableModel = (DefaultTableModel) TABULATION_TABLE.getModel();
                    tableModel.setRowCount(0);

                    try {
                        String query;
                        if (selectedCriteria.contains("(All)")) {
                            query = "SELECT s.criteria_title, c.name AS candidate_name, s.outOf, s.judge_score, s.judge_id, j.fullname AS judge_name "
                                    + "FROM scores s "
                                    + "JOIN candidate c ON s.candidate_id = c.id "
                                    + "JOIN judge j ON s.judge_id = j.judge_id "
                                    + "WHERE s.candidate_id = ?";

                        } else {
                            query = "SELECT s.criteria_title, c.name AS candidate_name, s.outOf, s.judge_score, s.judge_id, j.fullname AS judge_name "
                                    + "FROM scores s "
                                    + "JOIN candidate c ON s.candidate_id = c.id "
                                    + "JOIN judge j ON s.judge_id = j.judge_id "
                                    + "WHERE s.criteria_title = ? AND s.candidate_id = ?";
                        }

                        pst = conn.prepareStatement(query);

                        if (!selectedCriteria.contains("(All)")) {
                            pst.setString(1, selectedCriteria);
                            pst.setInt(2, Integer.parseInt(TABULATION_CANDIDATE_ID.getText()));
                        } else {
                            pst.setInt(1, Integer.parseInt(TABULATION_CANDIDATE_ID.getText()));

                        }
                        rs = pst.executeQuery();

                        while (rs.next()) {
                            String criteriaTitle = rs.getString("criteria_title");
                            String candidateName = rs.getString("candidate_name");
                            int outOf = rs.getInt("outOf");
                            double judgeScore = rs.getDouble("judge_score");
                            String judgeName = rs.getString("judge_name");

                            // Add the row to the table
                            tableModel.addRow(new Object[]{criteriaTitle, candidateName, outOf, judgeScore, judgeName});
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex);
                    }
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);

        }
    }

    private void OVERALL_WINNER_BYCATEGORY() {
        WINNER_CATEGORY_TITLE.setText("TEENAGER (MALE)");

        try {
            String selectedCategory = (String) CANDIDATE_CATEGORY_DROPDOWN_WINNERS.getSelectedItem();

            String query = "SELECT s.criteria_title, c.name AS candidate_name, c.image, s.outOf, s.judge_score, s.judge_id, j.fullname AS judge_name "
                    + "FROM scores s "
                    + "JOIN candidate c ON s.candidate_id = c.id "
                    + "JOIN judge j ON s.judge_id = j.judge_id "
                    + "WHERE c.category = ? AND c.gender = ?";
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

            Map<String, Double> candidateTotalScores = new HashMap<>();
            double overallTotalScore = 0.0;
            int judgeCount = 0;
            String winner = null;
            byte[] winnerImage = null;

            while (rs.next()) {
                String candidateName = rs.getString("candidate_name");
                double judgeScore = rs.getDouble("judge_score");
                winnerImage = rs.getBytes("image");

                // Update the total score for the current candidate
                if (candidateTotalScores.containsKey(candidateName)) {
                    double currentTotalScore = candidateTotalScores.get(candidateName);
                    currentTotalScore += judgeScore;
                    candidateTotalScores.put(candidateName, currentTotalScore);
                } else {
                    candidateTotalScores.put(candidateName, judgeScore);
                }

                judgeCount++;
            }

            // Calculate the average score for each candidate
            for (Map.Entry<String, Double> entry : candidateTotalScores.entrySet()) {
                String candidateName = entry.getKey();
                double totalScore = entry.getValue() / judgeCount;
                candidateTotalScores.put(candidateName, totalScore);
            }

            // Find the winner with the highest total score
            double highestTotalScore = -1.0;

            for (Map.Entry<String, Double> entry : candidateTotalScores.entrySet()) {
                String candidateName = entry.getKey();
                double totalScore = entry.getValue();

                if (totalScore > highestTotalScore) {
                    highestTotalScore = totalScore;
                    winner = candidateName;

                }
            }
            DecimalFormat decimalFormat = new DecimalFormat("0.0");

            overallTotalScore = highestTotalScore * candidateTotalScores.size();

            WINNER_CATEGORY.setText(winner);
            WINNER_CATEGORY_SCORES.setText(String.valueOf(decimalFormat.format(overallTotalScore)));

            // Set the candidate picture
            ImageIcon imageIcon = new ImageIcon(winnerImage);
            Image scaledImage = imageIcon.getImage().getScaledInstance(WINNER_IMAGE_WINNER.getWidth(), WINNER_IMAGE_WINNER.getHeight(), Image.SCALE_SMOOTH);
            WINNER_IMAGE_WINNER.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void CRITERIA_WINNER() {
        try {
            String selectedCategory = (String) CANDIDATE_CATEGORY_DROPDOWN_WINNERS.getSelectedItem();

            String query = "SELECT s.criteria_title, c.name AS candidate_name, c.category, s.outOf, s.judge_score, s.judge_id, j.fullname AS judge_name "
                    + "FROM scores s "
                    + "JOIN candidate c ON s.candidate_id = c.id "
                    + "JOIN judge j ON s.judge_id = j.judge_id ";

            if (!selectedCategory.equals("All")) {
                query += "WHERE c.category = ? AND c.gender = ?";
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
            } else {
                pst = conn.prepareStatement(query);
            }

            rs = pst.executeQuery();

            Map<String, Map<String, Double>> criterionScores = new HashMap<>();
            Map<String, String> criterionWinners = new HashMap<>();

            while (rs.next()) {
                String criteriaTitle = rs.getString("criteria_title");
                String candidateName = rs.getString("candidate_name");
                String category = rs.getString("category");
                double judgeScore = rs.getDouble("judge_score");

                // Retrieve the scores for the current criterion
                Map<String, Double> scores = criterionScores.getOrDefault(criteriaTitle, new HashMap<>());

                // Update the score for the current candidate
                double currentScore = scores.getOrDefault(candidateName, 0.0);
                currentScore += judgeScore;
                scores.put(candidateName, currentScore);

                // Update the scores map for the current criterion
                criterionScores.put(criteriaTitle, scores);

                // Store the category of the winner instead of the candidateName
                criterionWinners.put(criteriaTitle, category);
            }

            // Determine the winner for each criterion
            for (Map.Entry<String, Map<String, Double>> entry : criterionScores.entrySet()) {
                String criteriaTitle = entry.getKey();
                Map<String, Double> scores = entry.getValue();

                // Find the candidate with the highest score
                String winner = "";
                double highestScore = -1.0;

                for (Map.Entry<String, Double> scoreEntry : scores.entrySet()) {
                    String candidateName = scoreEntry.getKey();
                    double score = scoreEntry.getValue();

                    if (score > highestScore) {
                        highestScore = score;
                        winner = candidateName;
                    }
                }

                criterionWinners.put(criteriaTitle, winner);
            }

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Criteria Title");
            model.addColumn("Winner");

            for (Map.Entry<String, String> entry : criterionWinners.entrySet()) {
                String criteriaTitle = entry.getKey();
                String winner = entry.getValue();

                model.addRow(new Object[]{criteriaTitle, winner});
            }

            NGEKOY.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

}
