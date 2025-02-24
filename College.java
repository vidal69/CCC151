import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class College {
    private final String code;
    private final String name;
    private final List<Program> programs;

    private static final Map<String, College> COLLEGES_MAP = loadCollegesFromCSV();

    public College(String code, String name) {
        this.code = code;
        this.name = name;
        this.programs = new ArrayList<>();
    }

    public void addProgram(Program program) {
        programs.add(program);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public List<Program> getPrograms() { return programs; }

    @Override
    public String toString() {
        return "College{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", programs=" + programs +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        College college = (College) obj;
        return code.equals(college.code) && name.equals(college.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    private static Map<String, College> loadCollegesFromCSV() {
        Map<String, College> colleges = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("colleges.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 2) continue;

                String collegeCode = data[0].trim();
                String collegeName = data[1].trim();
                colleges.put(collegeCode, new College(collegeCode, collegeName));
            }
        } catch (IOException e) {
            System.err.println("Error reading colleges.csv: " + e.getMessage());
        }
        return colleges;
    }

    public static College getCollegeByCode(String code) {
        return COLLEGES_MAP.get(code);
    }

    public static Collection<College> getAllColleges() {
        return COLLEGES_MAP.values();
    }
}