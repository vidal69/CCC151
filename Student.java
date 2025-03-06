import java.util.regex.Pattern;

public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private String yearLevel;
    private String gender;
    private String program;

    private static final Pattern ID_PATTERN = Pattern.compile("\\d{4}-\\d{4}");

    public Student(String id, String firstName, String lastName, String yearLevel, String gender, String program) {
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("ID must be in format yyyy-nnnn");
        }
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.yearLevel = yearLevel;
        this.gender = gender;
        this.program = program;
    }

    public String getId() { return id; }
    public void setId(String id) {
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("ID must be in format yyyy-nnnn");
        }
        this.id = id;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String toCSV() {
        // Note: College removed from the CSV format
        return id + "," + firstName + "," + lastName + "," + yearLevel + "," + gender + "," + program;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", gender='" + gender + '\'' +
                ", program='" + program + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id != null ? id.equals(student.id) : student.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}