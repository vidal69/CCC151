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
        String selectedCollege = (String) filterCollegeBox.getSelectedItem();
        String selectedProgram = (String) filterProgramBox.getSelectedItem();
        filteredList = studentList.stream().filter(student -> {
            boolean matchesCollege = selectedCollege.equals("All") || student.getCollege().equals(selectedCollege);
            boolean matchesProgram = selectedProgram.equals("All") || student.getProgram().equals(selectedProgram);
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
                    collegeCodes.add(data[0]);
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
                    String programCode = data[0];
                    String collegeCode = data[2];
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
        setSize(800, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        idField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        yearLevelBox = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchStudent(searchField.getText().trim());
            }
        });
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
                    collegeBox.setSelectedItem(student.getCollege());
                    programBox.setModel(new DefaultComboBoxModel<>(getProgramsForCollege(student.getCollege())));
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
        JButton manageCollegesButton = new JButton("Manage Colleges & Programs");
        manageCollegesButton.addActionListener(e -> openCollegeProgramManager());
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
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.revalidate();
        buttonPanel.repaint();

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
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
        filterCollegeBox = new JComboBox<>(filterColleges);
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
                comparator = Comparator.comparing(Student::getCollege, String.CASE_INSENSITIVE_ORDER);
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
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
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
        studentList.add(new Student(id, firstName, lastName, yearLevel, gender, college, program));
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
            selectedStudent.setCollege(college);
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
        if (query.isEmpty()) {
            updateTable();
            return;
        }
        List<Student> results = studentList.stream()
                .filter(s -> s.getId().toLowerCase().contains(query.toLowerCase()) ||
                        s.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                        s.getLastName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
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
            for (int i = 0; i < collegeCodes.size(); i++) {
                if (student.getCollege().equalsIgnoreCase(collegeCodes.get(i))) {
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
            updateProgramsDropdown(); // Update programs dynamically
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

    private void openCollegeProgramManager() {
        SwingUtilities.invokeLater(() -> {
            CollegeProgramManager manager = new CollegeProgramManager();
            manager.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Reload the updated data after closing the manager
                    loadCollegesAndPrograms();
                    updateCollegeDropdown();
                    updateFilterDropdowns();
                }
            });
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDatabaseGUI());
    }

    private class StudentTableModel extends AbstractTableModel {
        private List<Student> studentList;
        private final String[] columnNames = {"ID", "First Name", "Last Name", "Year", "Gender", "College", "Program"};

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
                case 0: return student.getId();
                case 1: return student.getFirstName();
                case 2: return student.getLastName();
                case 3: return student.getYearLevel();
                case 4: return student.getGender();
                case 5: return student.getCollege();
                case 6: return student.getProgram();
                default: return null;
            }
        }
    }
}