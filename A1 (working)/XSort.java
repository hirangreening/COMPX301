// ID: 1522172
// Name: Hiran Greening

// Import statements
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This code reads plain text input from standard input, creates initial runs,and
 * performs a balanced k-way merge sort on the runs.
 */
public class XSort {

    /**
     * The main method reads the input from standard input, sorts the lines, and
     * writes the sorted lines to standard output.
     * 
     * @param args the command line arguments where args[0] is the run length, and
     *             args[1] is the merge factor (2: solo solution).
     */
    public static void main(String[] args) {

        // Check and validate command line arguments
        if (args.length < 1 || args.length > 2) {

            // Print usage message
            System.err.println("Usage: java XSort <runLength> [mergeFactor]");

            // Exit the program
            System.exit(1);
        }

        // Declare variables
        int runLength;

        // try-catch block
        try {

            // Parse run length and validate value
            runLength = Integer.parseInt(args[0]);

            // Check if run length is between 64 and 1024
            if (runLength < 64 || runLength > 1024) {

                // Throw an exception
                throw new IllegalArgumentException("Run length must be between 64 and 1024.");
            }

            // Catch exception
        } catch (IllegalArgumentException e) {

            // Print error message and exit the program
            System.err.println("Error: " + e.getMessage());
            System.exit(1);

            // Return from the method
            return;
        }


        // Declare and initialize merge factor
        int mergeFactor = 0;

        // Check if merge factor is provided
        if (args.length == 2) {

            // try-catch block
            try {
                // Parse merge factor and validate value
                mergeFactor = Integer.parseInt(args[1]);

                // Check if merge factor is not 2
                if (mergeFactor != 2) {

                    // Throw an exception
                    throw new IllegalArgumentException("Merge factor must be 2 (solo solution)");
                }

                // Catch exception
            } catch (IllegalArgumentException e) {

                // Print error message and exit the program
                System.err.println("Error: " + e.getMessage());
                System.exit(1);

                // Return from the method
                return;
            }
        }


        // try-catch block
        try {

            // Generate initial sorted runs of the specified length
            List<String> runFiles = createInitialRuns(runLength);

            // check if merge factor is 2
            if (mergeFactor == 2) {

                // Perform a balanced k-way merge sort on the runs
                performMerge(runFiles, mergeFactor);
            }

            // Catch exception
        } catch (IOException e) {

            // Print error message and exit the program
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }


    /**
     * Creates initial runs of the specified length by reading lines from standard
     * input, sorting them, and writing them to temporary files.
     * 
     * @param runLength The length of each run.
     * @return The list of paths to the run files.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If the run length is invalid.
     */
    private static List<String> createInitialRuns(int runLength) throws IOException {

        // Create a BufferedReader to read from standard input
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // List to store lines for each run
        List<String> lines = new ArrayList<>();

        // List to store paths of run files
        List<String> runFiles = new ArrayList<>();

        // declare string variable
        String line;

        // Read input line by line and create runs
        while ((line = reader.readLine()) != null) {

            // add line to list
            lines.add(line);

            // Check if the run length is reached
            if (lines.size() == runLength) {


                // Write the sorted run to a temporary file
                String runFile = writeSortedRun(lines);

                // Add the run file to the list
                runFiles.add(runFile);

                // Clear lines for the next run
                lines.clear();
            }
        }

        // cjheck if lines is not empty
        if (!lines.isEmpty()) {

            // Write the sorted run to a temporary file
            String runFile = writeSortedRun(lines);

            // Add the run file to the list
            runFiles.add(runFile);
        }

        // check if runFiles is empty
        if (runFiles.isEmpty()) {

            // Print message
            System.err.println("No runs created. The input file is empty.");

            // else verify runs
        } else {

            // Verify the sorted runs for correct sorting order and line count
            verifyRuns(runFiles, runLength);
        }   

        // Return the list of run files
        return runFiles;
    }

    /**
    * Writes the sorted run to a temporary file.
    *
    * @param lines The list of lines to sort.
    * @return The path of the run file.
    * @throws IOException If an I/O error occurs.
     */
    private static String writeSortedRun(List<String> lines) throws IOException {

        // Create a new heap with the size of the lines
        Heap heap = new Heap(lines.size());

        // for each line in lines
        for (String line : lines) {

            // Insert the line into the heap
            heap.insert(line);
        }

        // Sort the heap
        heap.heapsort();

        //  Convert the heap to a sorted array of lines
        String[] sortedLines = Arrays.stream(heap.getHeapArray())

                // Filter out null values
                .filter(Objects::nonNull)

                // Convert to an array of strings
                .toArray(String[]::new);

        // Reverse the array for A-Z order
        Collections.reverse(Arrays.asList(sortedLines));

        // Specify the directory for saving run files
        File directory = new File("/home/hiran/Documents/Trimester A 2025/COMPX301/A1/runs");

        // Check if the directory exists
        if (!directory.exists()) {

            // Create the directory if it doesn't exist
            directory.mkdirs();
        }

        // create file object to make run files with unique names
        File runFile = File.createTempFile("run_", ".txt", directory);

        // Write the sorted run to the run file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(runFile))) {

            // Write each line to the file
            for (String line : sortedLines) {

                // Write the line to the file
                writer.write(line);

                // Write a new line character
                writer.newLine();
            }
        }

        // Return the path of the run file
        return runFile.getAbsolutePath();
    }

    /**
     * Performs a balanced k-way merge sort on the runs.
     * 
     * @param runFiles    The list of sorted run files.
     * @param mergeFactor The merge factor.
     * @throws IOException If an I/O error occurs.
     * 
    */
    private static void performMerge(List<String> runFiles, int mergeFactor) throws IOException {

        // Variable to store the number of passes
        int pass = 0;


        // Merge runs until there is only one run left
        while (runFiles.size() > 1) {

            // List to store new run files
            List<String> newRunFiles = new ArrayList<>();

            // Merge runs in groups of 'mergeFactor'
            for (int i = 0; i < runFiles.size(); i += mergeFactor) {

                // List to store readers for the run files
                List<BufferedReader> readers = new ArrayList<>();

                // for each run file in the list of run files 
                for (int j = 0; j < mergeFactor && i + j < runFiles.size(); j++) {

                    // Add a new reader for the run file
                    readers.add(new BufferedReader(new FileReader(runFiles.get(i + j))));
                }

                // Create a new run file for the merged output
                File mergedRunFile = File.createTempFile("merged_run_", ".txt",
                        new File("/home/hiran/Documents/Trimester A 2025/COMPX301/A1/runs"));

                // Write the merged run to the new run file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedRunFile))) {

                    // Merge the runs
                    mergeKFiles(readers, writer);
                }

                // Add the new run file to the list
                newRunFiles.add(mergedRunFile.getAbsolutePath());

                // for all readers
                for (BufferedReader reader : readers) {

                    // Close the reader
                    reader.close();
                }
            }

            // Update the list of run files for the next pass
            runFiles = newRunFiles;
            pass++;
        }

        // Write the final sorted run to standard output
        try (BufferedReader reader = new BufferedReader(new FileReader(runFiles.get(0)))) {

            // Read and print each line from the final run file
            String line;

            // while line is not null
            while ((line = reader.readLine()) != null) {

                // Print the line
                System.out.println(line);
            }
        }
    }


    /**
     * Merges k sorted files into a single sorted file.
     * 
     * @param readers The list of readers for the input files.
     * @param writer  The writer for the output file.
     * @throws IOException If an I/O error occurs.
     * @return The path of the merged run file.
     */
    private static void mergeKFiles(List<BufferedReader> readers, BufferedWriter writer) throws IOException {

        // Create a heap to store the smallest lines from each file
        Heap heap = new Heap(readers.size());

        // Create FileLine array to store the current line and file index
        FileLine[] fileLines = new FileLine[readers.size()];

        // for each reader in readers list
        for (int i = 0; i < readers.size(); i++) {

            // Read the first line from each file
            String line = readers.get(i).readLine();

            // Check if the line is not null
            if (line != null) {

                // Store the line and file index in the FileLine array
                fileLines[i] = new FileLine(line, i);

                // Insert the line into the heap
                heap.insert(line);
            }
        }

        // while heap is not empty
        while (!heap.isEmpty()) {

            // Extract the smallest line from the heap
            String smallestLine = heap.remove();

            // Variable to store the file index of the smallest line
            int fileIndex = -1;

            // Find the corresponding file index for the smallest line
            for (int i = 0; i < fileLines.length; i++) {

                // Check if the line is not null and matches the smallest line
                if (fileLines[i] != null && fileLines[i].getLine().equals(smallestLine)) {

                    // Store the file index
                    fileIndex = fileLines[i].getFileIndex();

                    // break the loop
                    break;
                }
            }

            // Write the smallest line to the output file
            writer.write(smallestLine);

            // Write a new line character
            writer.newLine();

            // Read the next line from the file
            String nextLine = readers.get(fileIndex).readLine();

            // Check if the next line is not null
            if (nextLine != null) {

                // Update the file line for the file
                fileLines[fileIndex] = new FileLine(nextLine, fileIndex);

                // Insert the new line into the heap
                heap.insert(nextLine);

                // Check if the next line is null
            } else {

                // Mark this file as exhausted
                fileLines[fileIndex] = null;
            }
        }
    }

    /**
     * Class to store the current line and file index.
     * 
     * @param line     The current line.
     * @param fileIndex The file index.
     * 
     * @return The FileLine object.
     * 
     */
    private static class FileLine {

        // Declare variables
        private final String line;
        private final int fileIndex;


        // Constructor
        public FileLine(String line, int fileIndex) {

            // Initialise variables
            this.line = line;
            this.fileIndex = fileIndex;
        }

        /**
         * Get the current line.
         * 
         * @return The current line.
         */
        public String getLine() {

            // Return the line
            return line;
        }

        /*
        * Get the file index.
        *
        * @return The file index.
        */
            public int getFileIndex() {

            // Return the file index
            return fileIndex;
        }
    }

    /**
    * (Testing method I needed to verify the sorted runs for correct sorting order and line count).
    *
    * @param runFiles The list of sorted run files.
    * @param runLength The length of each run.
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

                    // break the loop
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
            // System.out.println("All runs are valid. Sorting and line count are correct
            // for all files.");
        } else {

            // Print error messages
            System.err.println("Validation failed for the following runs:");
            for (String error : errorMessages) {
                System.err.println(error);
            }
        }
    }

}