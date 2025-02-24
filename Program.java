import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Program {
    private final String code;
    private final String name;
    private final College college;
    private int studentCount;

    public Program(String code, String name, College college) {
        this.code = code;
        this.name = name;
        this.college = college;
        this.studentCount = 0;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public College getCollege() { return college; }

    @Override
    public String toString() {
        return "Program{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", college=" + college.getName() +
                ", students=" + studentCount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Program program = (Program) obj;
        return code.equals(program.code) &&
                name.equals(program.name) &&
                college.equals(program.college);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, college);
    }

    public static Map<String, Program> loadProgramsFromCSV(Map<String, College> colleges) {
        Map<String, Program> programs = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("programs.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 3) continue;

                String programCode = data[0].trim();
                String programName = data[1].trim();
                String collegeCode = data[2].trim();

                College college = colleges.get(collegeCode);
                if (college != null) {
                    Program program = new Program(programCode, programName, college);
                    programs.put(programCode, program);
                    college.addProgram(program);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading programs.csv: " + e.getMessage());
        }
        return programs;
    }
}