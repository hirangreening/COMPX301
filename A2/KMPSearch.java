// ID: 1522172
// Name: Hiran Greening
//
// This program implements the Knuth–Morris–Pratt (KMP) algorithm for substring search.
// When run with just a target string, it prints a comma-separated skip table (a DFA shift table)
// for that target as described in specification.
// When run with both a target and a filename, it searches the file for the target string
// and prints out each occurrence (for pair submissions, each occurrence is printed on its own line).

import java.util.*;
import java.io.*;

public class KMPSearch {

    public static void main(String[] args) {
        Map<Character, int[]> skipTable = new HashMap<>();
        String target = "";
        String filename = "";

        if (args.length == 1) {
            target = args[0];
            skipTable = generateSkipTable(target);
            printSkipTable(target, skipTable); // New function to print the skip table
            return;
        }

        if (args.length == 2) {
            target = args[0];
            filename = args[1];
            skipTable = generateSkipTable(target);
            searchFileWithSkipTable(filename, target, skipTable);
            return;
        }

        System.out.println("Usage: java KMPsearch \"target\" [filename.txt]");
    }

    private static void printSkipTable(String target, Map<Character, int[]> skipTable) {
        int m = target.length();

        System.out.print("*,");
        for (int i = 0; i < m; i++) {
            System.out.print(target.charAt(i));
            if (i < m - 1) System.out.print(",");
        }
        System.out.println();

        List<Character> sortedKeys = new ArrayList<>(skipTable.keySet());
        Collections.sort(sortedKeys);
        sortedKeys.remove(Character.valueOf('*')); // Remove '*' for separate handling

        for (Character c : sortedKeys) {
            int[] skips = skipTable.get(c);
            System.out.print(c + ",");
            for (int i = 0; i < m; i++) {
                System.out.print(skips[i]);
                if (i < m - 1) System.out.print(",");
            }
            System.out.println();
        }

        System.out.print("*,");
        int[] defaultSkips = skipTable.get('*');
        for (int i = 0; i < m; i++) {
            System.out.print(defaultSkips[i]);
            if (i < m - 1) System.out.print(",");
        }
        System.out.println();
    }

    /**
     * Generates and prints the KMP skip table (the DFA’s shift amounts)
     * in a comma-separated format. The table consists of:
     * - The first row: asterisk then the characters of the target pattern.
     * - One row for each unique character (from the target, in alphabetical order),
     * where each cell gives: (stateIndex+1) - nextState(stateIndex, letter)
     * - A final row (labeled with "*") for all characters not in the pattern, where
     * each cell simply shows (stateIndex+1).
     */
    private static Map<Character, int[]> generateSkipTable(String target) {
        if (target == null || target.isEmpty()) {
            System.out.println("Target string is empty.");
            return new HashMap<>(); // Return an empty map
        }

        int m = target.length();
        int[] prefix = buildKmpTable(target);
        Map<Character, int[]> skipTable = new HashMap<>();

        TreeSet<Character> uniqueChars = new TreeSet<>();
        for (char c : target.toCharArray()) {
            uniqueChars.add(c);
        }

        for (char c : uniqueChars) {
            int[] skips = new int[m];
            for (int j = 0; j < m; j++) {
                int next = nextState(target, j, c, prefix);
                int skip = (j + 1) - next;
                skips[j] = skip;
            }
            skipTable.put(c, skips);
        }

        int[] defaultSkips = new int[m];
        for (int i = 0; i < m; i++) {
            defaultSkips[i] = i + 1;
        }
        skipTable.put('*', defaultSkips);

        return skipTable;
    }

    /**
     * Computes the "next state" when at state q and the next character is c.
     * This is computed recursively using the prefix (π) table.
     * If target.charAt(q) equals c, the next state is simply q+1.
     * Otherwise, if q is 0, there is no proper prefix, so return 0.
     * Otherwise, recursively determine the next state from prefix[q-1].
     */
    private static int nextState(String target, int q, char c, int[] prefix) {
        if (q < target.length() && c == target.charAt(q))
            return q + 1;
        if (q == 0)
            return 0;
        return nextState(target, prefix[q - 1], c, prefix);
    }

 /**
     * Searches the file line by line for the first occurrence of the target string in each line.
     * If the target is found, prints the 1-based index of the first occurrence
     * followed by a colon and the line. Lines without the target are not printed.
     */
    // private static void searchFile(String filename, String target) {
    //     try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             int index = kmpSearchFirstOccurrence(line, target);
    //             if (index != -1) {
    //                 System.out.println(index + " " + line);
    //             }
    //         }
    //     } catch (IOException e) {
    //         System.out.println("Error reading file: " + e.getMessage());
    //     }
    // }

    private static void searchFileWithSkipTable(String filename, String target, Map<Character, int[]> skipTable) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<Integer> indices = kmpSearchWithSkipTable(line, target, skipTable);
                for (int index : indices) {
                    System.out.println(index + " " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private static List<Integer> kmpSearchWithSkipTable(String text, String pattern, Map<Character, int[]> skipTable) {
        List<Integer> indices = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        int i = 0; // Index for the text
        int j = 0; // Index for the pattern

        while (i <= n - m) {
            j = 0;
            while (j < m && i + j < n && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                indices.add(i + 1); // Match found
                i++; // Move to the next possible starting position
            } else if (i + j < n) {
                char mismatchedChar = text.charAt(i + j);
                int skip = 1; // Default skip

                if (skipTable.containsKey(mismatchedChar)) {
                    skip = skipTable.get(mismatchedChar)[j];
                } else {
                    skip = skipTable.get('*')[j];
                }
                i += skip;
            } else {
                break; // Reached end of text
            }
        }
        return indices;
    }


    // /**
    //  * Performs the KMP search on the given text for the pattern,
    //  * returning a list of 1-based indices for each occurrence.
    //  */
    // private static List<Integer> kmpSearch(String text, String pattern) {
    //     List<Integer> indices = new ArrayList<>();
    //     int[] prefix = buildKmpTable(pattern);
    //     int i = 0, j = 0;

    //     while (i < text.length()) {
    //         if (text.charAt(i) == pattern.charAt(j)) {
    //             i++;
    //             j++;
    //             if (j == pattern.length()) {
    //                 indices.add(i - j + 1); // Add the 1-based index of the match
    //                 j = prefix[j - 1]; // Use the prefix table to skip unnecessary comparisons
    //             }
    //         } else {
    //             if (j > 0) {
    //                 j = prefix[j - 1]; // Backtrack using the prefix table
    //             } else {
    //                 i++;
    //             }
    //         }
    //     }

    //     return indices;
    // }

    /**
     * Performs the KMP search on the given text for the first occurrence of the pattern,
     * returning the 1-based index of the first occurrence, or -1 if not found.
     * 
     * @param text The text to search in.
     * @param pattern The pattern to search for.
     * @return The 1-based index of the first occurrence, or -1 if not found.
     */
    // private static int kmpSearchFirstOccurrence(String text, String pattern) {
    //     int[] prefix = buildKmpTable(pattern);
    //     int i = 0, j = 0;

    //     while (i < text.length()) {
    //         if (text.charAt(i) == pattern.charAt(j)) {
    //             i++;
    //             j++;
    //             if (j == pattern.length()) {
    //                 return i - j + 1; // Return the 1-based index of the first match
    //             }
    //         } else {
    //             if (j > 0) {
    //                 j = prefix[j - 1];
    //             } else {
    //                 i++;
    //             }
    //         }
    //     }
    //     return -1; // Pattern not found in the text
    // }

    /**
     * Builds the KMP prefix (failure) table for the pattern.
     * prefix[i] holds the length of the longest proper prefix which is also a suffix for pattern[0..i].
     * 
     * @param pattern The pattern for which to build the prefix table.
     * @return The prefix table as an array of integers.
     */
    private static int[] buildKmpTable(String pattern) {
        int m = pattern.length();
        int[] table = new int[m];
        table[0] = 0; // The first value is always 0
        int j = 0;

        for (int i = 1; i < m; i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = table[j - 1]; // Backtrack using the prefix table
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            table[i] = j; // Store the length of the longest prefix-suffix
        }

        return table;
    }
}