import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class CollegeProgramManager extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;


    public CollegeProgramManager() {
        setTitle("College and Programs Manager");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadCollegeAndProgramData();

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteProgramButton = new JButton("Delete Program");
        JButton deleteCollegeButton = new JButton("Delete College");
        JButton saveButton = new JButton("Save");

        addButton.addActionListener(e -> addEntry());
        updateButton.addActionListener(e -> updateEntry());
        deleteProgramButton.addActionListener(e -> deleteProgram());
        deleteCollegeButton.addActionListener(e -> deleteCollege());
        saveButton.addActionListener(e -> saveDataToCSV());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteProgramButton);
        buttonPanel.add(deleteCollegeButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadCollegeAndProgramData() {
        tableModel.setColumnIdentifiers(new String[]{"College Code", "College Name", "Program Code", "Program Name"});
        try {
            Map<String, String> colleges = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader("colleges.csv"));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    colleges.put(data[0], data[1]);
                }
            }
            br.close();

            br = new BufferedReader(new FileReader("programs.csv"));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    String programCode = data[0];
                    String programName = data[1];
                    String collegeCode = data[2];
                    String collegeName = colleges.getOrDefault(collegeCode, "Unknown College");
                    tableModel.addRow(new String[]{collegeCode, collegeName, programCode, programName});
                }
            }
            br.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEntry() {
        String collegeCode;
        do {
            collegeCode = JOptionPane.showInputDialog(this, "Enter College Code:");
            if (collegeCode == null || collegeCode.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "College Code cannot be empty!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (collegeCode == null || collegeCode.trim().isEmpty());

        String collegeName;
        do {
            collegeName = JOptionPane.showInputDialog(this, "Enter College Name:");
            if (collegeName == null || collegeName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "College Name cannot be empty!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (collegeName == null || collegeName.trim().isEmpty());

        String programCode = JOptionPane.showInputDialog(this, "Enter Program Code (Optional):");
        String programName = JOptionPane.showInputDialog(this, "Enter Program Name (Optional):");

        tableModel.addRow(new String[]{collegeCode, collegeName, programCode != null ? programCode : "", programName != null ? programName : ""});
    }

    private void updateEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String collegeCode;
        do {
            collegeCode = JOptionPane.showInputDialog(this, "Update College Code:", tableModel.getValueAt(selectedRow, 0));
            if (collegeCode == null || collegeCode.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "College Code cannot be empty!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (collegeCode == null || collegeCode.trim().isEmpty());

        String collegeName;
        do {
            collegeName = JOptionPane.showInputDialog(this, "Update College Name:", tableModel.getValueAt(selectedRow, 1));
            if (collegeName == null || collegeName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "College Name cannot be empty!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (collegeName == null || collegeName.trim().isEmpty());

        String programCode = JOptionPane.showInputDialog(this, "Update Program Code (Optional):", tableModel.getValueAt(selectedRow, 2));
        String programName = JOptionPane.showInputDialog(this, "Update Program Name (Optional):", tableModel.getValueAt(selectedRow, 3));

        tableModel.setValueAt(collegeCode, selectedRow, 0);
        tableModel.setValueAt(collegeName, selectedRow, 1);
        tableModel.setValueAt(programCode != null ? programCode : "", selectedRow, 2);
        tableModel.setValueAt(programName != null ? programName : "", selectedRow, 3);
    }


    private void deleteProgram() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a program to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this program?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
        }
    }

    private void deleteCollege() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a college to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String collegeCode = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this college and all its programs?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                if (tableModel.getValueAt(i, 0).toString().equals(collegeCode)) {
                    tableModel.removeRow(i);
                }
            }
        }
    }

    private void saveDataToCSV() {
        try (PrintWriter collegeWriter = new PrintWriter(new FileWriter("colleges.csv"));
             PrintWriter programWriter = new PrintWriter(new FileWriter("programs.csv"))) {

            collegeWriter.println("College_Code,Name");
            programWriter.println("Program_Code,Name,College_Code");

            Set<String> savedColleges = new HashSet<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String collegeCode = tableModel.getValueAt(i, 0).toString();
                String collegeName = tableModel.getValueAt(i, 1).toString();
                String programCode = tableModel.getValueAt(i, 2).toString();
                String programName = tableModel.getValueAt(i, 3).toString();

                if (!savedColleges.contains(collegeCode)) {
                    collegeWriter.println(collegeCode + "," + collegeName);
                    savedColleges.add(collegeCode);
                }
                programWriter.println(programCode + "," + programName + "," + collegeCode);
            }
            JOptionPane.showMessageDialog(this, "Data saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CollegeProgramManager::new);
    }
}