import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CollegeProgramTest {
    public static void main(String[] args) {

        List<College> colleges = UniversityData.initializeColleges();
        Map<String, Integer> studentCounts = loadStudentCounts("students.csv");

        System.out.println("Testing College and Program Initialization...");
        System.out.println("==============================================\n");

        for (College college : colleges) {
            int totalStudents = 0;
            System.out.println("College: " + college.getName() + " (" + college.getCode() + ")");
            System.out.println("   Programs:");

            for (Program program : college.getPrograms()) {

                String key = college.getCode() + "-" + program.getCode();
                int count = studentCounts.getOrDefault(key, 0);
                program.setStudentCount(count);
                totalStudents += count;

                System.out.println("   " + program.getCode() + " [" + count + "] - " + program.getName());
            }
            System.out.println("   Total Students in College: " + totalStudents + "\n");
        }
    }

    private static Map<String, Integer> loadStudentCounts(String filename) {
        Map<String, Integer> studentCounts = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 7) continue;

                String collegeCode = data[5].trim();
                String programCode = data[6].trim();
                String key = collegeCode + "-" + programCode;

                studentCounts.put(key, studentCounts.getOrDefault(key, 0) + 1);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return studentCounts;
    }
}