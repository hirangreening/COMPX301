// ID: 1522172
// Name: Hiran Greening

// Import statements
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This code reads plain text input from standard input, creates initial runs,
 * and
 * performs a balanced k-way merge sort on the runs.
 */
public class XSort {

    /**
     * The main method reads the input from standard input, sorts the lines, and
     * writes the sorted lines to standard output.
     * 
     * @param args the command line arguments where args[0] is the run length, and
     *             args[1] is the merge factor (2 or 4).
     */
    public static void main(String[] args) {
        // Check and validate command line arguments
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java XSort <runLength> [mergeFactor]");
            System.exit(1);
        }

        int runLength;
        try {
            // Parse run length and validate range
            runLength = Integer.parseInt(args[0]);
            if (runLength < 64 || runLength > 1024) {
                throw new IllegalArgumentException("Run length must be between 64 and 1024.");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return;
        }

        int mergeFactor = 0; // Default to no merging
        if (args.length == 2) {
            try {
                // Parse merge factor and validate value
                mergeFactor = Integer.parseInt(args[1]);
                if (mergeFactor != 2 && mergeFactor != 4) {
                    throw new IllegalArgumentException("Merge factor must be 2 or 4.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
                return;
            }
        }

        try {
            // Step 1: Create initial runs
            List<String> runFiles = createInitialRuns(runLength);

            // Step 2: Perform merging if mergeFactor is provided
            if (mergeFactor > 0) {
                performMerge(runFiles, mergeFactor);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Reads lines from standard input, creates sorted runs of the specified length,
     * and writes each run to a separate temporary file.
     * 
     * @param runLength The length of each run.
     * @return A list of file paths to the temporary files containing the sorted
     *         runs.
     * @throws IOException If an I/O error occurs.
     */
    private static List<String> createInitialRuns(int runLength) throws IOException {
        // Create a BufferedReader to read from standard input
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        List<String> runFiles = new ArrayList<>();
        String line;

        // Read input line by line and create runs
        while ((line = reader.readLine()) != null) {
            lines.add(line);
            if (lines.size() == runLength) {
                // Sort and write a run when the specified length is reached
                String runFile = writeSortedRun(lines);
                runFiles.add(runFile);
                lines.clear(); // Clear lines for the next run
            }
        }

        // Write any remaining lines as a final run
        if (!lines.isEmpty()) {
            String runFile = writeSortedRun(lines);
            runFiles.add(runFile);
        }

        // After creating all runs
        if (runFiles.isEmpty()) {
            System.out.println("No runs created. The input file is empty.");
        } else {
            // Verify all runs for sorting and line count
            verifyRuns(runFiles, runLength);
        }

        return runFiles;
    }

    /**
     * Sorts a list of lines using heapsort and writes the sorted lines to a
     * specified directory.
     * 
     * @param lines The list of lines to sort.
     * @return The path to the file containing the sorted run.
     * @throws IOException If an I/O error occurs.
     */
    private static String writeSortedRun(List<String> lines) throws IOException {
        // Log the unsorted lines
        // System.out.println("Unsorted lines: " + lines);

        // Create a heap and sort the lines
        Heap heap = new Heap(lines.size());
        for (String line : lines) {
            heap.insert(line); // Insert all lines into the heap
        }
        heap.heapsort(); // Sort the heap

        // Reverse the sorted lines for A-Z order
        String[] sortedLines = Arrays.stream(heap.getHeapArray())
                .filter(Objects::nonNull) // Remove null values
                .toArray(String[]::new);
        Collections.reverse(Arrays.asList(sortedLines)); // Reverse for A-Z order
        // System.out.println("Sorted lines: " + Arrays.toString(sortedLines));

        // Specify the directory for saving run files
        File directory = new File("/home/hiran/Documents/Trimester A 2025/COMPX301/A1/runs");
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // Create the run file in the specified directory
        File runFile = File.createTempFile("run_", ".txt", directory);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(runFile))) {
            for (String line : sortedLines) {
                writer.write(line); // Write each line to the file
                writer.newLine();
            }
        }

        // Return the path of the run file
        return runFile.getAbsolutePath();
    }

    /**
     * Placeholder for merging logic.
     * 
     * @param runFiles    The list of sorted run files.
     * @param mergeFactor The number of ways to merge (2 or 4).
     * @throws IOException If an I/O error occurs.
     */
    private static void performMerge(List<String> runFiles, int mergeFactor) throws IOException {
        int pass = 0;

        // Continue merging until only one final sorted run is produced
        while (runFiles.size() > 1) {
            List<String> newRunFiles = new ArrayList<>();

            // Merge runs in groups of 'mergeFactor'
            for (int i = 0; i < runFiles.size(); i += mergeFactor) {
                List<BufferedReader> readers = new ArrayList<>();
                for (int j = 0; j < mergeFactor && i + j < runFiles.size(); j++) {
                    readers.add(new BufferedReader(new FileReader(runFiles.get(i + j))));
                }

                // Create a new run file for the merged output
                File mergedRunFile = File.createTempFile("merged_run_", ".txt",
                        new File("/home/hiran/Documents/Trimester A 2025/COMPX301/A1/runs"));
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedRunFile))) {
                    mergeKFiles(readers, writer);
                }

                // Add the new run file to the list
                newRunFiles.add(mergedRunFile.getAbsolutePath());

                // Close all readers
                for (BufferedReader reader : readers) {
                    reader.close();
                }
            }

            // Update the list of run files for the next pass
            runFiles = newRunFiles;
            pass++;
        }

        // Write the final sorted run to standard output
        try (BufferedReader reader = new BufferedReader(new FileReader(runFiles.get(0)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private static void mergeKFiles(List<BufferedReader> readers, BufferedWriter writer) throws IOException {
        // Create a heap to store the smallest lines from each file
        Heap heap = new Heap(readers.size());
        FileLine[] fileLines = new FileLine[readers.size()];

        // Initialize the heap with the first line from each file
        for (int i = 0; i < readers.size(); i++) {
            String line = readers.get(i).readLine();
            if (line != null) {
                fileLines[i] = new FileLine(line, i);
                heap.insert(line); // Insert the line into the heap
            }
        }

        // Merge lines from the files
        while (!heap.isEmpty()) {
            // Extract the smallest line from the heap
            String smallestLine = heap.remove();
            int fileIndex = -1;

            // Find the corresponding file index for the smallest line
            for (int i = 0; i < fileLines.length; i++) {
                if (fileLines[i] != null && fileLines[i].getLine().equals(smallestLine)) {
                    fileIndex = fileLines[i].getFileIndex();
                    break;
                }
            }

            // Write the smallest line to the output file
            writer.write(smallestLine);
            writer.newLine();

            // Read the next line from the file that provided the smallest line
            String nextLine = readers.get(fileIndex).readLine();
            if (nextLine != null) {
                fileLines[fileIndex] = new FileLine(nextLine, fileIndex);
                heap.insert(nextLine); // Insert the new line into the heap
            } else {
                fileLines[fileIndex] = null; // Mark this file as exhausted
            }
        }
    }

    // Helper class to store a line and its file index
    private static class FileLine {
        private final String line;
        private final int fileIndex;

        public FileLine(String line, int fileIndex) {
            this.line = line;
            this.fileIndex = fileIndex;
        }

        public String getLine() {
            return line;
        }

        public int getFileIndex() {
            return fileIndex;
        }
    }

    /**
     * Verifies the sorted runs for correct sorting order and line count.
     * 
     * @param runFiles  The list of sorted run files.
     * @param runLength The expected length of each run.
     * @throws IOException If an I/O error occurs.
     */
    private static void verifyRuns(List<String> runFiles, int runLength) throws IOException {

        // Validate each run file for sorting and line count
        boolean allValid = true;

        // List to store error messages
        List<String> errorMessages = new ArrayList<>();

        // Check each run file for sorting and line count
        for (String runFilePath : runFiles) {

            // Read all lines from the run file
            File runFile = new File(runFilePath);

            // Read all lines from the run file
            List<String> lines = Files.readAllLines(runFile.toPath());

            // Check if the file is sorted correctly
            boolean isSorted = true;

            // Check if the file is sorted correctly
            for (int i = 1; i < lines.size(); i++) {

                // Compare the current line with the previous line
                if (lines.get(i - 1).compareTo(lines.get(i)) > 0) {

                    // If current line is less than previous line, the file is not sorted
                    isSorted = false;

                    // Add error message and break the loop
                    errorMessages.add("Sorting error in " + runFile.getName() + " at line " + i + ": '"
                            + lines.get(i - 1) + "' > '" + lines.get(i) + "'");
                    break;
                }
            }

            // Check if the file has the correct number of lines
            if (lines.size() != runLength && runFiles.indexOf(runFilePath) != runFiles.size() - 1) {

                // Non-final runs should have exactly runLength lines
                allValid = false;

                // Add error message
                errorMessages.add("Line count mismatch in " + runFile.getName() + ": expected "
                        + runLength + ", but got " + lines.size());

                // Check if the file has the correct number of lines
            } else if (lines.size() > runLength) {

                // Final run can have less than or equal to runLength lines
                allValid = false;

                // Add error message
                errorMessages.add("Line count exceeds run length in " + runFile.getName() + ": got "
                        + lines.size() + " lines");
            }

            // If the file is not sorted, set allValid to false
            if (!isSorted) {
                allValid = false;
            }
        }

        // Print validation results
        if (allValid) {

            // Print success message
            // System.out.println("All runs are valid. Sorting and line count are correct for all files.");
        } else {

            // Print error messages
            System.err.println("Validation failed for the following runs:");
            for (String error : errorMessages) {
                System.err.println(error);
            }
        }
    }

}