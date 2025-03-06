import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Comparator;

public class CollegeManager extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField collegeCodeField;
    private JTextField collegeNameField;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;
    private JComboBox<String> sortCriteriaComboBox;

    private List<Object[]> allData = new ArrayList<>();

    public CollegeManager() {
        setTitle("College Manager");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("College Details"));
        inputPanel.add(new JLabel("College Code:"));
        collegeCodeField = new JTextField();
        inputPanel.add(collegeCodeField);
        inputPanel.add(new JLabel("College Name:"));
        collegeNameField = new JTextField();
        inputPanel.add(collegeNameField);
        topPanel.add(inputPanel);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Colleges"));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            public void changedUpdate(DocumentEvent e) { performSearch(); }
        });
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("By:"));
        searchCriteriaComboBox = new JComboBox<>(new String[]{"College Code", "College Name"});
        searchPanel.add(searchCriteriaComboBox);
        JButton clearSearchButton = new JButton("Clear Search");
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            updateTable(allData);
        });
        searchPanel.add(clearSearchButton);
        topPanel.add(searchPanel);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.setBorder(BorderFactory.createTitledBorder("Sort Colleges"));
        sortPanel.add(new JLabel("Sort by:"));
        sortCriteriaComboBox = new JComboBox<>(new String[]{"College Code", "College Name"});
        sortCriteriaComboBox.addActionListener(e -> performSort());
        sortPanel.add(sortCriteriaComboBox);
        topPanel.add(sortPanel);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"College Code", "College Name"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addEntry());
        updateButton.addActionListener(e -> updateEntry());
        deleteButton.addActionListener(e -> deleteEntry());
        saveButton.addActionListener(e -> saveData());
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                collegeCodeField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                collegeNameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            }
        });

        refreshData();
        setVisible(true);
    }

    private void loadCollegeData() {
        tableModel.setRowCount(0);
        allData.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("colleges.csv"))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    Object[] row = {code, name};
                    allData.add(row);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading colleges: " + e.getMessage());
        }
        updateTable(allData);
    }

    private void updateTable(List<Object[]> data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void refreshData() {
        loadCollegeData();
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if(query.isEmpty()){
            updateTable(allData);
            return;
        }
        String criteria = (String) searchCriteriaComboBox.getSelectedItem();
        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : allData) {
            if (criteria.equals("College Code") && row[0].toString().toLowerCase().contains(query)) {
                filtered.add(row);
            } else if (criteria.equals("College Name") && row[1].toString().toLowerCase().contains(query)) {
                filtered.add(row);
            }
        }
        updateTable(filtered);
    }

    private void performSort() {
        String criteria = (String) sortCriteriaComboBox.getSelectedItem();
        allData.sort(new Comparator<Object[]>() {
            @Override
            public int compare(Object[] a, Object[] b) {
                if (criteria.equals("College Code")) {
                    return a[0].toString().compareToIgnoreCase(b[0].toString());
                } else {
                    return a[1].toString().compareToIgnoreCase(b[1].toString());
                }
            }
        });
        updateTable(allData);
    }

    private boolean isCollegeCodeExists(String code) {
        for (Object[] row : allData) {
            if (row[0].toString().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    private void addEntry() {
        String code = collegeCodeField.getText().trim();
        String name = collegeNameField.getText().trim();
        if (code.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required!");
            return;
        }
        if (isCollegeCodeExists(code)) {
            JOptionPane.showMessageDialog(this, "College Code must be unique!");
            return;
        }
        Object[] row = {code, name};
        allData.add(row);
        updateTable(allData);
        clearFields();
    }

    private void updateEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to update!");
            return;
        }
        String newCode = collegeCodeField.getText().trim();
        String newName = collegeNameField.getText().trim();
        if (newCode.isEmpty() || newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required!");
            return;
        }
        Object[] originalRow = allData.get(selectedRow);
        String oldCode = originalRow[0].toString();
        if (!oldCode.equalsIgnoreCase(newCode) && isCollegeCodeExists(newCode)) {
            JOptionPane.showMessageDialog(this, "College Code must be unique!");
            return;
        }

        originalRow[0] = newCode;
        originalRow[1] = newName;
        updateTable(allData);
        clearFields();

        if (!oldCode.equalsIgnoreCase(newCode)) {
            cascadeUpdateCollegeCode(oldCode, newCode);
        }
    }

    private void deleteEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this college?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        Object[] removed = allData.remove(selectedRow);
        updateTable(allData);
        clearFields();
        cascadeDelete(removed[0].toString());
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("colleges.csv"))) {
            writer.println("College_Code,College_Name");
            for (Object[] row : allData) {
                writer.println(row[0] + "," + row[1]);
            }
            JOptionPane.showMessageDialog(this, "Data saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }

    private void clearFields() {
        collegeCodeField.setText("");
        collegeNameField.setText("");
    }


    private void cascadeDelete(String deletedCollegeCode) {

        List<String> programLines = new ArrayList<>();
        List<String> deletedProgramCodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("programs.csv"))) {
            String header = br.readLine();
            if (header != null) {
                programLines.add(header);
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String programCode = parts[0].trim();
                    String programName = parts[1].trim();
                    String programCollegeCode = parts[2].trim();
                    if (programCollegeCode.equalsIgnoreCase(deletedCollegeCode)) {
                        deletedProgramCodes.add(programCode);
                        programLines.add(programCode + "," + programName + ",N/A");
                    } else {
                        programLines.add(line);
                    }
                } else {
                    programLines.add(line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating programs: " + e.getMessage());
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("programs.csv"))) {
            for (String updatedLine : programLines) {
                writer.println(updatedLine);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving updated programs: " + e.getMessage());
            return;
        }

        List<String> studentLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("students.csv"))) {
            String header = br.readLine();
            if (header != null) {
                studentLines.add(header);
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String studentProgram = parts[5].trim();
                    if (deletedProgramCodes.contains(studentProgram)) {
                        parts[5] = "N/A";
                    }
                    studentLines.add(String.join(",", parts));
                } else {
                    studentLines.add(line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating students: " + e.getMessage());
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("students.csv"))) {
            for (String updatedLine : studentLines) {
                writer.println(updatedLine);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving updated students: " + e.getMessage());
        }
    }

    private void cascadeUpdateCollegeCode(String oldCode, String newCode) {
        List<String> programLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("programs.csv"))) {
            String header = br.readLine();
            if (header != null) {
                programLines.add(header);
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String programCode = parts[0].trim();
                    String programName = parts[1].trim();
                    String programCollegeCode = parts[2].trim();
                    if (programCollegeCode.equalsIgnoreCase(oldCode)) {
                        programLines.add(programCode + "," + programName + "," + newCode);
                    } else {
                        programLines.add(line);
                    }
                } else {
                    programLines.add(line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating programs: " + e.getMessage());
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("programs.csv"))) {
            for (String updatedLine : programLines) {
                writer.println(updatedLine);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving updated programs: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CollegeManager());
    }
}