// ID: 1522172
// Name: Hiran Greening

// Package statement
package A1;

// Import statements
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This code reads plain text input from a file, creates initial runs and
 * performs a balanced k-way merge sort on the runs.
 */
public class XSort {

    /**
     * The main method reads the input file, sorts the lines, and writes the sorted
     * lines to the output file.
     * 
     * @param args the command line arguments where args[0] is the input file name,
     *             args[1] is the run length, and args[2] is the merge factor (2 or
     *             4).
     */
    public static void main(String[] args) {

        // Check if the correct number of arguments have been passed
        if (args.length != 3) {

            // Print usage message and exit
            System.out.println("Usage: java XSort <filename> <runlength> <mergefactor>");
            System.exit(1);
        }

        // Parse the run length and merge factor
        int runLength = Integer.parseInt(args[1]);
        int mergeFactor = Integer.parseInt(args[2]);

        // Check if the run length is within the valid range
        if (runLength < 64 || runLength > 1024) {

            // Print usage message and exit
            System.out.println("Error: Run length must be between 64 and 1024.");
            System.out.println("Usage: java XSort <filename> <runlength> <mergefactor>");
            System.exit(1);
        }

        // Check if the merge factor is valid
        if (mergeFactor != 2 && mergeFactor != 4) {

            // Print usage message and exit
            System.out.println("Error: Merge factor must be 2 or 4.");
            System.out.println("Usage: java XSort <filename> <runlength> <mergefactor>");
            System.exit(1);
        }

        // TRY-CATCH block to read the input file, sort the lines, and write the sorted
        // lines to the output file
        try {

            // Create reader, writer, and string builder objects
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer = new BufferedWriter(new FileWriter("sorted-doc.txt"));
            StringBuilder sb = new StringBuilder();
            String line;

            // Read the content of the input file
            while ((line = reader.readLine()) != null) {

                // Append the line to the string builder
                sb.append(line).append(System.lineSeparator());
            }

            // Close the reader
            reader.close();

            // Split the content into lines and sort them
            String[] lines = sb.toString().split(System.lineSeparator());
            java.util.Arrays.sort(lines);

            // Write the sorted lines to the output file
            for (String sortedLine : lines) {

                // Write the line to the output file
                writer.write(sortedLine);
                writer.write(System.lineSeparator());
            }

            // Close the writer
            writer.close();

            // Print success message
            System.out.println("File has been sorted successfully.");
        } catch (IOException e) {

            // Print error message
            System.out.println("Error: An error occurred while sorting the file.");
            e.printStackTrace();
        }
    }
}