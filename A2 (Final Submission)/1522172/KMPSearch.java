// ID: 1522172
// Name: Hiran Greening

// Import statements
import java.util.*;
import java.io.*;

/**
 * Implements the KMP string search algorithm.
 *
 * Modes of operation:
 * 1. "target": Prints the KMP skip table.
 * 2. "target" filename.txt: Searches the file, printing lines with the target
 * and the 1-based index of the first occurrence.
 *
 * The skip table is stored (Map<Character, int[]>) and used for searching.
 */
public class KMPSearch {

    /**
     * Main method to run the KMP search algorithm.
     * It can either print the skip table or search a file for the target string.
     *
     * @param args Command line arguments: target string and optional filename.
     */
    public static void main(String[] args) {

        // Create map to hold the skip table
        Map<Character, int[]> skipTable = new HashMap<>();

        // initialise variables for target and filename
        String target = "";
        String filename = "";

        // Check if only target is provided
        if (args.length == 1) {

            // get target from args
            target = args[0];

            // create skip table for target
            skipTable = generateSkipTable(target);

            // call method to print skip table
            printSkipTable(target, skipTable);
            return;
        }

        // Check if target and filename are provided
        if (args.length == 2) {

            // get target and filename from args
            target = args[0];
            filename = args[1];

            // create skip table for target
            skipTable = generateSkipTable(target);

            // call method to search file using the skip table
            searchFileWithSkipTable(filename, target, skipTable);

            // return from main
            return;
        }

        // If no valid arguments are provided, print usage message
        System.out.println("Usage: java KMPsearch \"target\" [filename.txt]");
    }

    /**
     * Prints the KMP skip table in a comma-separated format.
     * 
     * @param target    target string for which the skip table is generated.
     * @param skipTable the skip table generated for the target string.
     */
    private static void printSkipTable(String target, Map<Character, int[]> skipTable) {

        // get target length
        int m = target.length();

        // print the header row (asterisk)
        System.out.print("*,");

        // loop through each index in target
        for (int i = 0; i < m; i++) {

            // print character at index i
            System.out.print(target.charAt(i));

            // if not the last character, print a comma
            if (i < m - 1)
                System.out.print(",");
        }

        // print a new line
        System.out.println();

        // get sorted list of keys from the skip table
        List<Character> sortedKeys = new ArrayList<>(skipTable.keySet());

        // sort the keys alphabetically (asc)
        Collections.sort(sortedKeys);

        // exclude '*' from alphabetical sort
        sortedKeys.remove(Character.valueOf('*'));

        // loop through each character in sorted keys
        for (Character c : sortedKeys) {

            // get the skips for the character
            int[] skips = skipTable.get(c);

            // print the character, followed by a comma
            System.out.print(c + ",");

            // loop through each skip value
            for (int i = 0; i < m; i++) {

                // print the skip value
                System.out.print(skips[i]);

                // if not the last skip value
                if (i < m - 1)

                    // print a comma
                    System.out.print(",");
            }

            // print a new line
            System.out.println();
        }

        // print asterisk, followed by a comma
        System.out.print("*,");

        // get the default skip values for '*' from the skip table
        int[] defaultSkips = skipTable.get('*');

        // loop through index in target
        for (int i = 0; i < m; i++) {

            // print the default skip value
            System.out.print(defaultSkips[i]);

            // if not the last default skip value
            if (i < m - 1)

                // print a comma
                System.out.print(",");
        }

        // print a new line
        System.out.println();
    }

    /**
     * Generates the KMP skip table for the given target string.
     *
     * @param target The target string for which to generate the skip table.
     * @return A map where each character in the target maps to an array of skip
     *         values.
     */
    private static Map<Character, int[]> generateSkipTable(String target) {

        // Check if target is null or empty
        if (target == null || target.isEmpty()) {

            // Print error message
            System.out.println("Target string is empty.");

            // return an empty map
            return new HashMap<>();
        }

        // get target length
        int m = target.length();

        // build prefix table for target
        int[] prefix = buildKmpTable(target);

        // initialise skip table
        Map<Character, int[]> skipTable = new HashMap<>();

        // initalise set for unique characters
        TreeSet<Character> uniqueChars = new TreeSet<>();

        // loop through each character in target
        for (char c : target.toCharArray()) {

            // add char to unique set
            uniqueChars.add(c);
        }

        // loop through each unique character
        for (char c : uniqueChars) {

            // initalise skip arr for char
            int[] skips = new int[m];

            // loop through each index in target
            for (int j = 0; j < m; j++) {

                // calculate next state (using prefix table)
                int next = nextState(target, j, c, prefix);

                // calculate skip value
                int skip = (j + 1) - next;

                // store skip value in skips array
                skips[j] = skip;
            }

            // store skips for char in skip table
            skipTable.put(c, skips);
        }

        // initialise default skip array
        int[] defaultSkips = new int[m];

        // loop through each index in target
        for (int i = 0; i < m; i++) {

            // set default skip value
            defaultSkips[i] = i + 1;
        }

        // store default skips
        skipTable.put('*', defaultSkips);

        // return the skip table
        return skipTable;
    }

    /**
     * Computes the "next state" in the KMP automaton.
     * Uses the prefix table to determine the next state based on the current state
     * and the character being checked.
     * 
     * @param target The target string.
     * @param q      The current state (index in the target string).
     * @param c      The character to check.
     * @param prefix The prefix table for the target string.
     * @return The next state based on the current state and character.
     */
    private static int nextState(String target, int q, char c, int[] prefix) {

        // Check if the character matches the target at the current state
        if (q < target.length() && c == target.charAt(q))

            // return the next state (q + 1)
            return q + 1;

        // No match at start of pattern
        if (q == 0)

            // stay at state 0
            return 0;

        // recursive call to backtrack using the prefix table
        return nextState(target, prefix[q - 1], c, prefix);
    }

    /**
     * Searches file for the target string using the KMP algorithm with the skip
     * table.
     * 
     * @param filename  The name of the file to search.
     * @param target    The target string to search for.
     * @param skipTable The skip table generated for the target string.
     */
    private static void searchFileWithSkipTable(String filename, String target, Map<Character, int[]> skipTable) {

        // try with resources to read the file
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            // declare line variable
            String line;

            // while there are lines to read
            while ((line = reader.readLine()) != null) {

                // KMP search (using skip table) for the target in the line, storing indices in
                // a list.
                List<Integer> indices = kmpSearchWithSkipTable(line, target, skipTable);

                // if the target was found in the line
                if (!indices.isEmpty()) {

                    // print the 1-based index of the first occurrence and the line
                    System.out.println(indices.get(0) + " " + line);
                }
            }

            // catch any IO exceptions
        } catch (IOException e) {

            // print error message
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Performs KMP search on the given text using the provided pattern and skip
     * table.
     * 
     * @param text      the text to search in.
     * @param pattern   the pattern to search for.
     * @param skipTable the skip table generated for the pattern.
     * @return a list of indices (1-based) where the pattern occurs in the text.
     */
    private static List<Integer> kmpSearchWithSkipTable(String text, String pattern, Map<Character, int[]> skipTable) {

        // initialise list for indices of pattern occurrences
        List<Integer> indices = new ArrayList<>();

        // get text and pattern lengths
        int n = text.length();
        int m = pattern.length();

        // initialise text index
        int i = 0;

        // initialise pattern index
        int j = 0;

        // loop through text until the end
        while (i <= n - m) {

            // set pattern index to 0
            j = 0;

            // while within pattern bounds and characters match
            while (j < m && i + j < n && text.charAt(i + j) == pattern.charAt(j)) {

                // move to next character in pattern
                j++;
            }

            // check if full pattern is found
            if (j == m) {

                // add index to list
                indices.add(i + 1);

                // increment text index
                i++;

                // if mismatch occurs
            } else if (i + j < n) {

                // get the mismatched character
                char mismatchedChar = text.charAt(i + j);

                // set skip to 1 (default)
                int skip = 1;

                // check if mismatched character is in skip table
                if (skipTable.containsKey(mismatchedChar)) {

                    // get the skip value for the mismatched character
                    skip = skipTable.get(mismatchedChar)[j];

                    // if the mismatched character is not in the skip table
                } else {

                    // get the skip value for '*'
                    skip = skipTable.get('*')[j];
                }

                // increment text index by the skip value (to skip characters)
                i += skip;

                // if end of the text is reached
            } else {

                // break the loop
                break;
            }
        }

        // return list of indices (where the pattern occurs in the text).
        return indices;
    }

    /**
     * Builds the KMP prefix (failure) table for the pattern.
     * prefix[i] holds the length of the longest proper prefix which is also a
     * suffix for pattern[0..i].
     * 
     * @param pattern The pattern for which to build the prefix table.
     * @return The prefix table as an array of integers.
     */
    private static int[] buildKmpTable(String pattern) {

        // get pattern length
        int m = pattern.length();

        // initialise prefix table of size m
        int[] table = new int[m];

        // set first value of prefix table to 0
        table[0] = 0;

        // set variable for prefix index
        int j = 0;

        // loop through pattern from index 1 to m - 1
        for (int i = 1; i < m; i++) {

            // while there is a mismatch and j > 0
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {

                // backtrack using the prefix table
                j = table[j - 1];
            }

            // if there is a match
            if (pattern.charAt(i) == pattern.charAt(j)) {

                // increment prefix index
                j++;
            }

            // store the prefix length in the table
            table[i] = j;
        }

        // return the prefix table
        return table;
    }
}
