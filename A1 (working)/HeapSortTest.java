import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HeapSortTest {
    public static void main(String[] args) {
        try {
            // Read the input file
            BufferedReader reader = new BufferedReader(new FileReader("test.txt"));
            List<String> lines = new ArrayList<>();
            String line;

            // Collect all lines from the file
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();

            // Convert the list to an array for sorting
            String[] data = lines.toArray(new String[0]);

            // Apply heapsort using your Heap class
            Heap heap = new Heap(data.length);
            heap.heapify(data);
            heap.heapsort();

            // Write the sorted output to a new file
            BufferedWriter writer = new BufferedWriter(new FileWriter("HeapSortedTest.txt"));
            for (String sortedLine : heap.getHeapArray()) {
                writer.write(sortedLine);
                writer.newLine();
            }
            writer.close();

            System.out.println("Heapsort completed successfully. Sorted data written to HeapSortedTest.txt");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
