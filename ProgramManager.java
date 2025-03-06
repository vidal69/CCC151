import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgramManager extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField programCodeField;
    private JTextField programNameField;
    private JComboBox<String> collegeCodeComboBox;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;
    private JComboBox<String> sortCriteriaComboBox;

    private List<Object[]> allData = new ArrayList<>();

    public ProgramManager() {
        setTitle("Program Manager");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Program Details"));
        inputPanel.add(new JLabel("Program Code:"));
        programCodeField = new JTextField();
        inputPanel.add(programCodeField);
        inputPanel.add(new JLabel("Program Name:"));
        programNameField = new JTextField();
        inputPanel.add(programNameField);
        inputPanel.add(new JLabel("College Code:"));
        collegeCodeComboBox = new JComboBox<>();
        collegeCodeComboBox.setEditable(false);
        inputPanel.add(collegeCodeComboBox);
        topPanel.add(inputPanel);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Programs"));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            public void changedUpdate(DocumentEvent e) { performSearch(); }
        });
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("By:"));
        searchCriteriaComboBox = new JComboBox<>(new String[]{"Program Code", "Program Name", "College Code"});
        searchPanel.add(searchCriteriaComboBox);
        JButton clearSearchButton = new JButton("Clear Search");
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            updateTable(allData);
        });
        searchPanel.add(clearSearchButton);
        topPanel.add(searchPanel);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.setBorder(BorderFactory.createTitledBorder("Sort Programs"));
        sortPanel.add(new JLabel("Sort by:"));
        sortCriteriaComboBox = new JComboBox<>(new String[]{"Program Code", "Program Name", "College Code"});
        sortCriteriaComboBox.addActionListener(e -> performSort());
        sortPanel.add(sortCriteriaComboBox);
        topPanel.add(sortPanel);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Program Code", "Program Name", "College Code"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addEntry());
        updateButton.addActionListener(e -> updateEntry());
        deleteButton.addActionListener(e -> deleteEntry());
        saveButton.addActionListener(e -> saveData());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                programCodeField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                programNameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                collegeCodeComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            }
        });

        refreshData();
        setVisible(true);
    }

    private void loadProgramData() {
        tableModel.setRowCount(0);
        allData.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("programs.csv"))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    String college = parts[2].trim();
                    Object[] row = {code, name, college};
                    allData.add(row);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading program data: " + ex.getMessage());
        }
        updateTable(allData);
    }

    private void updateTable(List<Object[]> data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
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
            if (criteria.equals("Program Code") && row[0].toString().toLowerCase().contains(query)) {
                filtered.add(row);
            } else if (criteria.equals("Program Name") && row[1].toString().toLowerCase().contains(query)) {
                filtered.add(row);
            } else if (criteria.equals("College Code") && row[2].toString().toLowerCase().contains(query)) {
                filtered.add(row);
            }
        }
        updateTable(filtered);
    }

    private void performSort() {
        String criteria = (String) sortCriteriaComboBox.getSelectedItem();
        allData.sort((a, b) -> {
            if(criteria.equals("Program Code")){
                return a[0].toString().compareToIgnoreCase(b[0].toString());
            } else if(criteria.equals("Program Name")){
                return a[1].toString().compareToIgnoreCase(b[1].toString());
            } else if(criteria.equals("College Code")){
                return a[2].toString().compareToIgnoreCase(b[2].toString());
            }
            return 0;
        });
        updateTable(allData);
    }

    private void refreshData() {
        loadProgramData();
        updateCollegeCodeComboBox();
    }

    private void updateCollegeCodeComboBox() {
        Set<String> codes = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("colleges.csv"))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length >= 1) {
                    codes.add(parts[0].trim());
                }
            }
        } catch (IOException ex) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                codes.add(tableModel.getValueAt(i, 2).toString());
            }
        }
        collegeCodeComboBox.setModel(new DefaultComboBoxModel<>(codes.toArray(new String[0])));
        collegeCodeComboBox.setEditable(false);
    }

    private boolean isProgramCodeExists(String code) {
        for (Object[] row : allData) {
            if (row[0].toString().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    private void validateNewProgram(String code, String name, String collegeCode) throws Exception {
        if (code.isEmpty() || name.isEmpty() || collegeCode.isEmpty()) {
            throw new Exception("All fields are required!");
        }
        if (isProgramCodeExists(code)) {
            throw new Exception("Program Code must be unique!");
        }
        if (!isCollegeCodeValid(collegeCode)) {
            throw new Exception("Selected College Code is not valid!");
        }
    }

    private boolean isCollegeCodeValid(String code) {
        ComboBoxModel<String> model = collegeCodeComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    private void addEntry() {
        String code = programCodeField.getText().trim();
        String name = programNameField.getText().trim();
        String collegeCode = (String) collegeCodeComboBox.getSelectedItem();
        if(collegeCode != null) {
            collegeCode = collegeCode.trim();
        } else {
            collegeCode = "";
        }
        try {
            validateNewProgram(code, name, collegeCode);
            Object[] row = {code, name, collegeCode};
            allData.add(row);
            updateTable(allData);
            clearFields();
            updateCollegeCodeComboBox();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to update!");
            return;
        }
        String newCode = programCodeField.getText().trim();
        String newName = programNameField.getText().trim();
        String newCollegeCode = (String) collegeCodeComboBox.getSelectedItem();
        if(newCollegeCode != null) {
            newCollegeCode = newCollegeCode.trim();
        } else {
            newCollegeCode = "";
        }
        try {
            if (newCode.isEmpty() || newName.isEmpty() || newCollegeCode.isEmpty()) {
                throw new Exception("All fields are required!");
            }
            Object[] originalRow = allData.get(selectedRow);
            if (!originalRow[0].toString().equalsIgnoreCase(newCode) && isProgramCodeExists(newCode)) {
                throw new Exception("Program Code must be unique!");
            }
            if (!isCollegeCodeValid(newCollegeCode)) {
                throw new Exception("Selected College Code is not valid!");
            }
            originalRow[0] = newCode;
            originalRow[1] = newName;
            originalRow[2] = newCollegeCode;
            updateTable(allData);
            clearFields();
            updateCollegeCodeComboBox();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this program?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String deletedProgramCode = tableModel.getValueAt(selectedRow, 0).toString();
        allData.remove(selectedRow);
        updateTable(allData);
        clearFields();
        cascadeDeleteProgram(deletedProgramCode);
        updateCollegeCodeComboBox();
    }

    private void cascadeDeleteProgram(String deletedProgramCode) {
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
                    if (studentProgram.equalsIgnoreCase(deletedProgramCode)) {
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
            for (String s : studentLines) {
                writer.println(s);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving updated students: " + e.getMessage());
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("programs.csv"))) {
            writer.println("Program_Code,Program_Name,College_Code");
            for (Object[] row : allData) {
                writer.println(row[0] + "," + row[1] + "," + row[2]);
            }
            JOptionPane.showMessageDialog(this, "Data saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }

    private void clearFields() {
        programCodeField.setText("");
        programNameField.setText("");
        collegeCodeComboBox.setSelectedIndex(-1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProgramManager());
    }
}