import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;

public class StudentDatabaseGUI extends JFrame {
    private JTextField idField, firstNameField, lastNameField, searchField;
    private JComboBox<String> yearLevelBox, genderBox, collegeBox, programBox;
    private JTable studentTable;
    private StudentTableModel tableModel;
    private JComboBox<String> searchCriteriaComboBox;
    private List<Student> studentList = new ArrayList<>();
    private JLabel studentCountLabel;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> filterCollegeBox;
    private JComboBox<String> filterProgramBox;
    private boolean sortAscending = true;



    private List<Student> filteredList = new ArrayList<>();


    private static final Pattern ID_PATTERN = Pattern.compile("\\d{4}-\\d{4}");

    private String[] getProgramsForCollege(String collegeCode) {
        return programsMap.getOrDefault(collegeCode, new ArrayList<>()).toArray(new String[0]);
    }

    private void updateProgramDropdown() {
        String selectedCollegeCode = (String) filterCollegeBox.getSelectedItem();
        if (selectedCollegeCode.equals("All")) {
            filterProgramBox.setModel(new DefaultComboBoxModel<>(new String[]{"All"}));
        } else {
            List<String> programs = programsMap.getOrDefault(selectedCollegeCode, new ArrayList<>());
            List<String> filterPrograms = new ArrayList<>();
            filterPrograms.add("All");
            filterPrograms.addAll(programs);
            filterProgramBox.setModel(new DefaultComboBoxModel<>(filterPrograms.toArray(new String[0])));
        }
    }

    private void applyFilter() {
        String selectedCollege = (filterCollegeBox.getSelectedItem() != null) ? filterCollegeBox.getSelectedItem().toString().trim() : "All";
        String selectedProgram = (filterProgramBox.getSelectedItem() != null) ? filterProgramBox.getSelectedItem().toString().trim() : "All";

        filteredList = studentList.stream().filter(student -> {
            boolean matchesCollege = true;
            if (!selectedCollege.equalsIgnoreCase("All")) {
                String studentCollege = getCollegeFromProgram(student.getProgram()).trim();
                matchesCollege = studentCollege.equalsIgnoreCase(selectedCollege);
            }
            boolean matchesProgram = selectedProgram.equalsIgnoreCase("All") ||
                    student.getProgram().trim().equalsIgnoreCase(selectedProgram);
            return matchesCollege && matchesProgram;
        }).collect(Collectors.toList());

        updateTable(filteredList);
    }

    private List<String> collegeCodes = new ArrayList<>();
    private Map<String, List<String>> programsMap = new HashMap<>();
    private Map<String, String> collegeCodeMap = new HashMap<>();

    private void loadCollegesAndPrograms() {
        collegeCodes.clear();
        programsMap.clear();

        try (BufferedReader br = new BufferedReader(new FileReader("colleges.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    collegeCodes.add(data[0].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Colleges CSV not found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("programs.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    String programCode = data[0].trim();
                    String collegeCode = data[2].trim();
                    programsMap.computeIfAbsent(collegeCode, k -> new ArrayList<>()).add(programCode);
                }
            }
        } catch (IOException e) {
            System.out.println("Programs CSV not found.");
        }
    }

    private void updateProgramsDropdown() {
        String selectedCollegeCode = (String) collegeBox.getSelectedItem();
        System.out.println("Selected College Code: " + selectedCollegeCode);
        System.out.println("Programs Map: " + programsMap);

        if (selectedCollegeCode != null) {
            List<String> programs = programsMap.getOrDefault(selectedCollegeCode, new ArrayList<>());
            System.out.println("Programs Found: " + programs);

            programBox.setModel(new DefaultComboBoxModel<>(programs.toArray(new String[0])));
        }
    }

    public StudentDatabaseGUI() {
        setTitle("Student Database Management");
        setSize(800, 870);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        idField = new JTextField();
        idField.addKeyListener(idNumberKeyListener);
        firstNameField = new JTextField();
        firstNameField.addKeyListener(letterOnlyKeyListener);
        lastNameField = new JTextField();
        lastNameField.addKeyListener(letterOnlyKeyListener);
        yearLevelBox = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        loadCollegesAndPrograms();

        collegeBox = new JComboBox<>(collegeCodes.toArray(new String[0]));

        programBox = new JComboBox<>();


        collegeBox.addActionListener(e -> updateProgramsDropdown());

        if (!collegeCodes.isEmpty()) {
            collegeBox.setSelectedIndex(0);
            updateProgramsDropdown();
        }

        inputPanel.add(new JLabel("ID (YYYY-NNNN):"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Year Level:"));
        inputPanel.add(yearLevelBox);
        inputPanel.add(new JLabel("Gender:"));
        inputPanel.add(genderBox);
        inputPanel.add(new JLabel("College:"));
        inputPanel.add(collegeBox);
        inputPanel.add(new JLabel("Program:"));
        inputPanel.add(programBox);


        tableModel = new StudentTableModel(studentList);
        studentTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(studentTable);
        studentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Student student = tableModel.getStudentAt(selectedRow);
                    idField.setText(student.getId());
                    firstNameField.setText(student.getFirstName());
                    lastNameField.setText(student.getLastName());
                    yearLevelBox.setSelectedItem(student.getYearLevel());
                    genderBox.setSelectedItem(student.getGender());

                    String inferredCollege = getCollegeFromProgram(student.getProgram());
                    if (inferredCollege != null) {
                        collegeBox.setSelectedItem(inferredCollege);
                        programBox.setModel(new DefaultComboBoxModel<>(getProgramsForCollege(inferredCollege)));
                    }
                    programBox.setSelectedItem(student.getProgram());
                }
            }
        });




        studentCountLabel = new JLabel("Total Students: 0");
        studentCountLabel.setForeground(Color.RED);
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(studentCountLabel, BorderLayout.WEST);


        studentList = CSVHandler.loadFromCSV();
        tableModel.setStudentList(studentList);
        updateTable();


        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton clearButton = new JButton("Clear Fields");
        JButton demographicsButton = new JButton("Generate Demographics");
        JButton manageCollegesButton = new JButton("Manage Colleges");
        JButton manageProgramsButton = new JButton("Manage Programs");
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        manageCollegesButton.addActionListener(e -> openCollegeManager());
        manageProgramsButton.addActionListener(e -> openProgramManager());
        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        saveButton.addActionListener(e -> saveToCSV());
        clearButton.addActionListener(e -> clearFields());
        demographicsButton.addActionListener(e -> generateDemographicsReport());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(demographicsButton);
        buttonPanel.add(manageCollegesButton);
        buttonPanel.add(manageProgramsButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.revalidate();
        buttonPanel.repaint();

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchStudent(searchField.getText().trim());
            }
        });
        searchCriteriaComboBox = new JComboBox<>(new String[]{"ID", "First Name", "Last Name"});

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(new JLabel(" By:"));
        searchPanel.add(searchCriteriaComboBox);
        searchPanel.add(searchField, BorderLayout.CENTER);

        sortComboBox = new JComboBox<>(new String[]{"ID", "First Name", "Last Name", "Year Level", "Gender", "College", "Program"});
        JButton sortButton = new JButton("Sort");

        JPanel sortPanel = new JPanel(new BorderLayout());
        sortPanel.add(new JLabel("Sort By: "), BorderLayout.WEST);
        sortPanel.add(sortComboBox, BorderLayout.CENTER);
        sortPanel.add(sortButton, BorderLayout.EAST);


        String[] filterColleges = new String[collegeCodes.size() + 1];
        filterColleges[0] = "All";
        for (int i = 0; i < collegeCodes.size(); i++) {
            filterColleges[i + 1] = collegeCodes.get(i);
        }
        filterCollegeBox = new JComboBox<>(filterColleges);
        filterCollegeBox.setModel(new DefaultComboBoxModel<>(filterColleges));


        filterProgramBox = new JComboBox<>(new String[]{"All"});

        filterCollegeBox.addActionListener(e -> {
            updateProgramDropdown();
            applyFilter();
        });
        filterProgramBox.addActionListener(e -> applyFilter());

        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filter by: "));
        filterPanel.add(filterCollegeBox);
        filterPanel.add(new JLabel("Program: "));
        filterPanel.add(filterProgramBox);



        JButton clearFilterButton = new JButton("Clear Filter");
        clearFilterButton.addActionListener(e -> {
            filterCollegeBox.setSelectedIndex(0);
            updateProgramDropdown();
            filterProgramBox.setSelectedIndex(0);
            filteredList.clear();
            updateTable();
        });
        filterPanel.add(clearFilterButton);

        JButton toggleSortButton = new JButton("Toggle Asc/Desc");


        sortButton.addActionListener(e -> sortStudents());
        toggleSortButton.addActionListener(e -> {
            sortAscending = !sortAscending;
            sortStudents();
        });

        filterPanel.add(toggleSortButton, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(searchPanel);
        bottomPanel.add(tableScroll);
        bottomPanel.add(statusPanel);
        bottomPanel.add(sortPanel);
        bottomPanel.add(filterPanel);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        add(panel);
        setVisible(true);
    }

    private void refreshData() {
        studentList = CSVHandler.loadFromCSV();
        filteredList.clear();
        updateTable(studentList);
        searchField.setText("");
    }

    private void sortStudents() {
        String selectedSortCriteria = (String) sortComboBox.getSelectedItem();
        List<Student> currentList = (!filteredList.isEmpty()) ? new ArrayList<>(filteredList) : new ArrayList<>(studentList);
        Comparator<Student> comparator;

        switch (selectedSortCriteria) {
            case "ID":
                comparator = Comparator.comparing(Student::getId);
                break;
            case "First Name":
                comparator = Comparator.comparing(Student::getFirstName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Last Name":
                comparator = Comparator.comparing(Student::getLastName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Year Level":
                comparator = Comparator.comparingInt(s -> Integer.parseInt(s.getYearLevel()));
                break;
            case "Gender":
                comparator = Comparator.comparing(Student::getGender, String.CASE_INSENSITIVE_ORDER);
                break;
            case "College":
                comparator = Comparator.comparing(s -> getCollegeFromProgram(s.getProgram()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Program":
                comparator = Comparator.comparing(Student::getProgram, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                comparator = Comparator.comparing(Student::getId);
        }

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        currentList = currentList.stream().sorted(comparator).collect(Collectors.toList());
        updateTable(currentList);
    }

    private void addStudent() {
        String id = idField.getText().trim();
        idField.addKeyListener(idNumberKeyListener);

        String firstName = firstNameField.getText().trim();
        firstNameField.addKeyListener(letterOnlyKeyListener);

        String lastName = lastNameField.getText().trim();
        lastNameField.addKeyListener(letterOnlyKeyListener);

        String yearLevel = (String) yearLevelBox.getSelectedItem();
        String gender = (String) genderBox.getSelectedItem();
        String college = (String) collegeBox.getSelectedItem();
        String program = (String) programBox.getSelectedItem();

        if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            JOptionPane.showMessageDialog(this, "ID format is invalid! Use YYYY-NNNN.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean exists = studentList.stream().anyMatch(s -> s.getId().equals(id));
        if (exists) {
            JOptionPane.showMessageDialog(this, "ID already exists! Please enter a unique ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        studentList.add(new Student(id, firstName, lastName, yearLevel, gender, program));
        updateTable();
        clearFields();
    }

    private void updateStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            String newId = idField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String yearLevel = (String) yearLevelBox.getSelectedItem();
            String gender = (String) genderBox.getSelectedItem();
            String college = (String) collegeBox.getSelectedItem();
            String program = (String) programBox.getSelectedItem();

            if (newId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ID_PATTERN.matcher(newId).matches()) {
                JOptionPane.showMessageDialog(this, "ID format is invalid! Use YYYY-NNNN.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Student selectedStudent = tableModel.getStudentAt(selectedRow);
            if (selectedStudent == null) {
                JOptionPane.showMessageDialog(this, "Selected student not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean exists = studentList.stream().anyMatch(s -> !s.equals(selectedStudent) && s.getId().equals(newId));
            if (exists) {
                JOptionPane.showMessageDialog(this, "ID already exists! Please enter a unique ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedStudent.setId(newId);
            selectedStudent.setFirstName(firstName);
            selectedStudent.setLastName(lastName);
            selectedStudent.setYearLevel(yearLevel);
            selectedStudent.setGender(gender);
            selectedStudent.setProgram(program);
            tableModel.fireTableRowsUpdated(selectedRow, selectedRow);
            JOptionPane.showMessageDialog(this, "Student updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "No student selected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        int[] selectedRows = studentTable.getSelectedRows();
        if (selectedRows.length > 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to delete the selected students?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                List<Student> toRemove = new ArrayList<>();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    Student student = tableModel.getStudentAt(selectedRows[i]);
                    if (student != null) {
                        toRemove.add(student);
                    }
                }
                studentList.removeAll(toRemove);
                applyFilter();
                clearFields();
                JOptionPane.showMessageDialog(this, "Selected students deleted successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No students selected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Student> students) {
        tableModel.setStudentList(students);
        studentCountLabel.setText("Total Students: " + students.size());
    }

    private void updateTable() {
        updateTable(studentList);
    }

    private void clearFields() {
        idField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        yearLevelBox.setSelectedIndex(0);
        genderBox.setSelectedIndex(0);
        collegeBox.setSelectedIndex(0);
        programBox.setModel(new DefaultComboBoxModel<>(getProgramsForCollege((String) collegeBox.getSelectedItem())));
    }

    private void searchStudent(String query) {
        String selectedCollegeFilter = (String) filterCollegeBox.getSelectedItem();
        String selectedProgramFilter = (String) filterProgramBox.getSelectedItem();

        if (query.isEmpty()) {
            if (!"All".equals(selectedCollegeFilter) || !"All".equals(selectedProgramFilter)) {
                applyFilter();
            } else {
                updateTable(studentList);
            }
            return;
        }

        List<Student> sourceList = ("All".equals(selectedCollegeFilter) && "All".equals(selectedProgramFilter))
                ? studentList
                : filteredList.isEmpty() ? studentList : filteredList;

        String criteria = (String) searchCriteriaComboBox.getSelectedItem();
        List<Student> results = sourceList.stream().filter(student -> {
            String lowerQuery = query.toLowerCase();
            switch (criteria) {
                case "ID":
                    return student.getId().toLowerCase().contains(lowerQuery);
                case "First Name":
                    return student.getFirstName().toLowerCase().contains(lowerQuery);
                case "Last Name":
                    return student.getLastName().toLowerCase().contains(lowerQuery);
                default:
                    return false;
            }
        }).collect(Collectors.toList());

        updateTable(results);
    }

    private void saveToCSV() {
        CSVHandler.saveToCSV(studentList);
        JOptionPane.showMessageDialog(this, "Data saved successfully!");
    }

    private void generateDemographicsReport() {
        int totalStudents = studentList.size();
        int maleCount = (int) studentList.stream().filter(s -> s.getGender().equalsIgnoreCase("Male")).count();
        int femaleCount = (int) studentList.stream().filter(s -> s.getGender().equalsIgnoreCase("Female")).count();
        int[] collegeCounts = new int[collegeCodes.size()];
        int[] yearLevelCounts = new int[4];

        for (Student student : studentList) {
            String inferredCollege = getCollegeFromProgram(student.getProgram());
            for (int i = 0; i < collegeCodes.size(); i++) {
                if (inferredCollege.equalsIgnoreCase(collegeCodes.get(i))) {
                    collegeCounts[i]++;
                }
            }
            int yearLevelIndex = Integer.parseInt(student.getYearLevel()) - 1;
            if (yearLevelIndex >= 0 && yearLevelIndex < 4) {
                yearLevelCounts[yearLevelIndex]++;
            }
        }

        double malePercentage = (maleCount * 100.0) / totalStudents;
        double femalePercentage = (femaleCount * 100.0) / totalStudents;
        StringBuilder report = new StringBuilder();
        report.append("Total Students: ").append(totalStudents).append("\n\n");
        report.append("Gender Distribution:\n");
        report.append("  Male: ").append(maleCount).append(" (").append(String.format("%.2f", malePercentage)).append("%)\n");
        report.append("  Female: ").append(femaleCount).append(" (").append(String.format("%.2f", femalePercentage)).append("%)\n\n");
        report.append("College Distribution:\n");
        for (int i = 0; i < collegeCodes.size(); i++) {
            double percentage = (collegeCounts[i] * 100.0) / totalStudents;
            report.append("  ").append(collegeCodes.get(i)).append(": ").append(collegeCounts[i])
                    .append(" (").append(String.format("%.2f", percentage)).append("%)\n");
        }
        report.append("\nYear Level Distribution:\n");
        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
        for (int i = 0; i < yearLevelCounts.length; i++) {
            double percentage = (yearLevelCounts[i] * 100.0) / totalStudents;
            report.append("  ").append(yearLevels[i]).append(": ").append(yearLevelCounts[i])
                    .append(" (").append(String.format("%.2f", percentage)).append("%)\n");
        }
        JOptionPane.showMessageDialog(this, report.toString(), "Demographics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateCollegeDropdown() {
        collegeBox.setModel(new DefaultComboBoxModel<>(collegeCodes.toArray(new String[0])));
        collegeBox.addActionListener(e -> updateProgramsDropdown());

        if (!collegeCodes.isEmpty()) {
            collegeBox.setSelectedIndex(0);
            updateProgramsDropdown();
        }
    }

    private void updateFilterDropdowns() {
        String[] filterColleges = new String[collegeCodes.size() + 1];
        filterColleges[0] = "All";
        for (int i = 0; i < collegeCodes.size(); i++) {
            filterColleges[i + 1] = collegeCodes.get(i);
        }

        filterCollegeBox.setModel(new DefaultComboBoxModel<>(filterColleges));
        updateProgramDropdown();
    }

    private void openCollegeManager() {
        SwingUtilities.invokeLater(() -> {
            CollegeManager collegeManager = new CollegeManager();
            collegeManager.setVisible(true);
            collegeManager.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadCollegesAndPrograms();
                    updateCollegeDropdown();
                    updateFilterDropdowns();
                }
            });
        });
    }

    private void openProgramManager() {
        SwingUtilities.invokeLater(() -> {
            ProgramManager programManager = new ProgramManager();
            programManager.setVisible(true);
            programManager.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadCollegesAndPrograms();
                    updateCollegeDropdown();
                    updateFilterDropdowns();
                }
            });
        });
    }

    private KeyAdapter letterOnlyKeyListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (!Character.isLetter(c) && c != ' ') {
                e.consume();
            }
        }
    };

    private int lastKeyCode = 0;

    private KeyAdapter idNumberKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            lastKeyCode = e.getKeyCode();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (Character.isISOControl(c)) {
                return;
            }
            if (!Character.isDigit(c) && c != '-') {
                e.consume();
                return;
            }

            JTextField tf = (JTextField) e.getComponent();
            String text = tf.getText();
            int caretPos = tf.getCaretPosition();

            StringBuilder sb = new StringBuilder(text);
            sb.insert(caretPos, c);
            String newText = sb.toString();

            if (!newText.matches("\\d{0,4}(-?\\d{0,4})?")) {
                e.consume();
                return;
            }
            if (c == '-' && caretPos != 4) {
                e.consume();
                return;
            }
            if (newText.length() > 9) {
                e.consume();
                return;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField tf = (JTextField) e.getComponent();
            String text = tf.getText();
            if (lastKeyCode != KeyEvent.VK_BACK_SPACE && text.length() == 4 && !text.contains("-")) {
                tf.setText(text + "-");
                tf.setCaretPosition(tf.getText().length());
            }
        }
    };

    private String getCollegeFromProgram(String program) {
        if (program == null || program.equalsIgnoreCase("null") || program.isEmpty()) {
            return "";
        }
        for (Map.Entry<String, List<String>> entry : programsMap.entrySet()) {
            if (entry.getValue().contains(program)) {
                return entry.getKey();
            }
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDatabaseGUI());
    }

    private class StudentTableModel extends AbstractTableModel {
        private List<Student> studentList;
        private final String[] columnNames = {"ID", "First Name", "Last Name", "Year", "Gender", "Program"};

        public StudentTableModel(List<Student> studentList) {
            this.studentList = studentList;
        }

        public void setStudentList(List<Student> studentList) {
            this.studentList = studentList;
            fireTableDataChanged();
        }

        public Student getStudentAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < studentList.size()) {
                return studentList.get(rowIndex);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return studentList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Student student = studentList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return student.getId();
                case 1:
                    return student.getFirstName();
                case 2:
                    return student.getLastName();
                case 3:
                    return student.getYearLevel();
                case 4:
                    return student.getGender();
                case 5:
                    return student.getProgram();
                default:
                    return null;
            }
        }
    }
}