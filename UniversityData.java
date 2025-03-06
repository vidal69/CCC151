import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UniversityData {
    private static final String COLLEGES_CSV = "colleges.csv";
    private static final String PROGRAMS_CSV = "programs.csv";

    public static List<College> initializeColleges() {
        Map<String, College> colleges = loadColleges();
        loadPrograms(colleges);
        return new ArrayList<>(colleges.values());
    }

    private static Map<String, College> loadColleges() {
        Map<String, College> colleges = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(COLLEGES_CSV))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 2) continue; // Skip invalid rows

                String collegeCode = data[0].trim();
                String collegeName = data[1].trim();
                colleges.put(collegeCode, new College(collegeCode, collegeName));
            }
        } catch (IOException e) {
            System.err.println("Error reading " + COLLEGES_CSV + ": " + e.getMessage());
        }
        return colleges;
    }

    private static void loadPrograms(Map<String, College> colleges) {
        try (BufferedReader br = new BufferedReader(new FileReader(PROGRAMS_CSV))) {
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
                    college.addProgram(new Program(programCode, programName, college));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + PROGRAMS_CSV + ": " + e.getMessage());
        }
    }
}