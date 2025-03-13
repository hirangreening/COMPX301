// ID: 1522172
// Name: Hiran Greening

// Import statements
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This code reads plain text input from standard input, creates initial runs
 * and
 * performs a balanced k-way merge sort on the runs.
 */
public class XSort {

    /**
     * The main method reads the input from standard input, sorts the lines, and
     * writes the sorted
     * lines to standard output.
     * 
     * @param args the command line arguments where args[0] is the run length, and
     *             args[1] is the merge factor (2 or 4).
     */
    public static void main(String[] args) {

        // Check if the correct number of arguments have been passed
        if (args.length != 2) {

            // Print usage message and exit
            System.out.println("Usage: java XSort <runlength> <mergefactor>");
            System.exit(1);
        }

        // Parse the run length and merge factor
        int runLength = Integer.parseInt(args[0]);
        int mergeFactor = Integer.parseInt(args[1]);

        // Check if the run length is within the valid range
        if (runLength < 64 || runLength > 1024) {

            // Print usage message and exit
            System.out.println("Error: Run length must be between 64 and 1024.");
            System.out.println("Usage: java XSort <runlength> <mergefactor>");
            System.exit(1);
        }

        // Check if the merge factor is valid
        if (mergeFactor != 2 && mergeFactor != 4) {

            // Print usage message and exit
            System.out.println("Error: Merge factor must be 2 or 4.");
            System.out.println("Usage: java XSort <runlength> <mergefactor>");
            System.exit(1);
        }

        // TRY-CATCH block to read the input from standard input, sort the lines, and
        // write the sorted
        // lines to standard output
        try {

            // Create reader and writer objects
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
            List<String> lines = new ArrayList<>();
            String line;
            int lineCount = 0;

            // Read the content from standard input
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                lineCount++;
                if (lineCount == runLength) {
                    writeRunToOutput(lines, writer);
                    lines.clear();
                    lineCount = 0;
                }
            }

            // Write remaining lines if any
            if (!lines.isEmpty()) {
                writeRunToOutput(lines, writer);
            }

            // Close the writer
            writer.close();

            // Print success message
            System.out.println("Initial runs have been created successfully.");
        } catch (IOException e) {

            // Print error message
            System.out.println("Error: An error occurred while creating initial runs.");
            e.printStackTrace();
        }
    }

    private static void writeRunToOutput(List<String> lines, BufferedWriter writer) throws IOException {
        // Create a Heap and sort the data
        Heap heap = new Heap(lines.size());
        heap.heapify(lines.toArray(new String[0])); // Convert list to array and heapify
        heap.heapsort(); // Sort the heap

        // Write the sorted data from the heap's array to the output
        for (String sortedLine : heap.getHeapArray()) { // Use the getter to access heapArray
            writer.write(sortedLine);
            writer.write(System.lineSeparator());
        }
    }

}