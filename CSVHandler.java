import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class CSVHandler {
    private static final String FILE_NAME = "students.csv";
    private static final Logger LOGGER = Logger.getLogger(CSVHandler.class.getName());

    public static void saveToCSV(List<Student> studentList) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_NAME))) {
            writer.write("ID,First Name,Last Name,Year Level,Gender,Program");
            writer.newLine();

            for (Student student : studentList) {
                writer.write(String.join(",",
                        student.getId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getYearLevel(),
                        student.getGender(),
                        student.getProgram()));
                writer.newLine();
            }

            LOGGER.info("Data saved successfully to " + FILE_NAME);
        } catch (IOException e) {
            LOGGER.severe("Error saving file: " + e.getMessage());
        }
    }

    public static List<Student> loadFromCSV() {
        List<Student> studentList = new ArrayList<>();
        Path filePath = Paths.get(FILE_NAME);

        if (!Files.exists(filePath)) {
            LOGGER.warning("File not found: " + FILE_NAME);
            return studentList;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line = reader.readLine(); // Read header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) {
                    studentList.add(new Student(
                            data[0].trim(),
                            data[1].trim(),
                            data[2].trim(),
                            data[3].trim(),
                            data[4].trim(),
                            data[5].trim()));
                } else {
                    LOGGER.warning("Skipping invalid CSV line: " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error reading file: " + e.getMessage());
        }
        return studentList;
    }
}