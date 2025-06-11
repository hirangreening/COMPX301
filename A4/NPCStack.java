// Name: Hiran Greening
// ID: 1522172

// Import Statements
import java.io.*;
import java.util.*;

/**
 * This class implements a simulated annealing algorithm to stack boxes
 * in a way that maximizes the total height of the stack.
 */
public class NPCStack {

    /**
     * Main method to run the NPCStack program.
     * 
     * @param args Command line arguments: input file, initial temperature, cooling
     *             rate, and optional runs.
     */
    public static void main(String[] args) {

        // Check for correct number of arguments
        if (args.length < 3 || args.length > 4) {

            // print usage message
            System.out.println("Usage: java NPCStack <input_file> <initial_temperature> <cooling_rate> [runs]");
            return;
        }

        // Parse command line arguments
        String filename = args[0];
        int initialTemperature = 0;
        double coolingRate = 0.0;
        int runs = 1;

        // try-catch block
        try {

            // parse initial temperature
            initialTemperature = Integer.parseInt(args[1]);

            // parse cooling rate
            coolingRate = Double.parseDouble(args[2]);

            // check if runs arg is provided
            if (args.length == 4) {

                // parse runs
                runs = Integer.parseInt(args[3]);
            }

            // Catch exceptions for number format errors
        } catch (NumberFormatException e) {

            // Print error message and exit
            System.out.println(
                    "Error: initial_temperature must be an integer, cooling_rate a real number, runs an integer.");
            return;
        }

        // check if initial temperature is valid
        if (initialTemperature <= 0) {

            // Print error message and exit
            System.out.println("Error: initial_temperature must be > 0.");
            return;
        }

        // check if cooling rate is valid
        if (coolingRate < 0.1 || coolingRate > initialTemperature) {

            // Print error message and exit
            System.out.println("Error: cooling_rate must be in the range 0.1 <= cooling_rate <= initial_temperature.");
            return;
        }

        // check if runs is valid
        if (runs <= 0) {

            // Print error message and exit
            System.out.println("Error: runs must be > 0.");
            return;
        }

        // Create an instance of NPCStack
        NPCStack npcStack = new NPCStack();

        // Initialise a list of box objects by reading the input file
        List<Box> boxes = npcStack.readInputFile(filename);

        // Declae box list to hold the best stack
        List<Box> bestStack = null;

        // set best height to 0
        int bestHeight = 0;

        // loop for the number of runs
        for (int i = 0; i < runs; i++) {

            // Generate a candidate stack using simulated annealing
            List<Box> candidate = npcStack.simulatedAnnealing(boxes, initialTemperature, coolingRate);

            // Get the height of the candidate stack
            int height = npcStack.getStackHeight(candidate);

            // If this is the first run or the candidate is taller than the best so far
            if (bestStack == null || height > bestHeight) {

                // Update the best stack (found so far)
                bestStack = candidate;

                // Update the best height (found so far)
                bestHeight = height;
            }
        }

        // Print the best stack found
        npcStack.printStack(bestStack);
    }

    /**
     * Reads the input file and generates all orientations of boxes.
     * 
     * @param filename The name of the input file containing box dimensions.
     * @return A list of Box objects representing all orientations of the boxes.
     */
    public List<Box> readInputFile(String filename) {

        // List to hold all box orientations
        List<Box> allOrientations = new ArrayList<>();

        // set boxId to 0
        int boxId = 0;

        // Try to read the file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            // set line variable
            String line;

            // Read each line from the file (while there is a line)
            while ((line = br.readLine()) != null) {

                // split the line into parts
                String[] parts = line.trim().split("\\s+");

                // Check if the line has exactly 3 parts (dimensions)
                if (parts.length == 3) {

                    // try-catch block
                    try {

                        // intialise array of integers to hold dimensions
                        int[] dimensions = new int[3];

                        // loop through each part
                        for (int i = 0; i < 3; i++) {

                            // set the dimension by parsing the part
                            dimensions[i] = Integer.parseInt(parts[i]);

                            // check if the dimension is valid (positive)
                            if (dimensions[i] <= 0) {

                                // throw number format exception
                                throw new NumberFormatException();
                            }
                        }

                        // add all orientations of the box to the list with the current boxId
                        allOrientations.addAll(Box.generateOrientations(dimensions, boxId));

                        // Increment boxId for the next box
                        boxId++;

                        // Catch exceptions for number format errors
                    } catch (NumberFormatException e) {

                        // Print error message and continue to the next line
                        System.out.println("Error: Invalid dimensions in line: " + line);
                    }
                }
            }

            // Catch exceptions for file reading errors
        } catch (IOException e) {

            // Print error message if file cannot be read
            e.printStackTrace();
        }

        // Return the list of all box orientations
        return allOrientations;
    }

    /**
     * Simulated Annealing algorithm to find the best stack of boxes.
     * 
     * @param boxes              The list of all box orientations.
     * @param initialTemperature The initial temperature for the annealing process.
     * @param coolingRate        The rate at which the temperature decreases.
     * @return The best stack of boxes found during the annealing process.
     */
    public List<Box> simulatedAnnealing(List<Box> boxes, int initialTemperature, double coolingRate) {

        // initialise current solution with an initial candidate stack
        List<Box> currentSolution = generateInitialSolution(boxes);

        // initialise best solution with the current solution
        List<Box> bestSolution = new ArrayList<>(currentSolution);

        // Set the temperature for the annealing process
        double temperature = initialTemperature;

        // Loop until the temperature is reduced to 1
        while (temperature > 1) {

            // initialise a new solution by getting a neighbor solution
            List<Box> newSolution = getNeighborSolution(currentSolution, boxes, (int) temperature);

            // check if stack height of new solution is greater than current solution
            if (getStackHeight(newSolution) > getStackHeight(currentSolution)) {

                // Accept the new solution (set it as current solution)
                currentSolution = newSolution;

                // check if stack height of current solution is greater than best solution
                if (getStackHeight(currentSolution) > getStackHeight(bestSolution)) {

                    // Update the best solution
                    bestSolution = new ArrayList<>(currentSolution);
                }

                // else if new solution is worse, accept it with a probability based on
                // temperature
            } else {

                // get the acceptance probability based on the difference in stack heights and
                // temperature
                double acceptanceProbability = Math
                        .exp((getStackHeight(currentSolution) - getStackHeight(newSolution)) / temperature);

                // check if acceptance probability is greater than a random number
                if (Math.random() < acceptanceProbability) {

                    // Accept the new solution even if it's worse
                    currentSolution = newSolution;
                }
            }

            // Decrease the temperature according to the cooling rate
            temperature -= coolingRate;
        }

        // return the best solution found
        return bestSolution;
    }

    /**
     * Generates an initial solution by stacking boxes based on their dimensions.
     *
     * @param allOrientations The list of all box orientations.
     * @return A list representing the initial stack of boxes.
     */
    private List<Box> generateInitialSolution(List<Box> allOrientations) {

        // Create a new list to sort all box orientations
        List<Box> sorted = new ArrayList<>(allOrientations);

        // Sort boxes by decreasing base area (width * length), then by height if areas
        // are equal
        sorted.sort((a, b) -> {

            // Calculate base area for box a
            int areaA = a.getDimensions()[0] * a.getDimensions()[1];

            // Calculate base area for box b
            int areaB = b.getDimensions()[0] * b.getDimensions()[1];

            // If areas are different
            if (areaB != areaA) {

                // sort by area (larger first)
                return Integer.compare(areaB, areaA);
            }

            // If areas are the same, sort by height (taller first)
            return Integer.compare(b.getDimensions()[2], a.getDimensions()[2]);
        });

        // List to hold the initial stack
        List<Box> stack = new ArrayList<>();

        // Set to keep track of used box IDs (to avoid reusing the same box)
        Set<Integer> usedIds = new HashSet<>();

        // loop through each candidate box in the sorted list
        for (Box candidate : sorted) {

            // check if the candidate box has already been used
            if (usedIds.contains(candidate.getId()))

                // skip (continue to the next candidate)
                continue;

            // check if stack is empty or if the candidate can be placed on top of the last
            // box in the stack
            if (stack.isEmpty() || canPlaceOnTop(stack.get(stack.size() - 1), candidate)) {

                // Add the candidate box to the stack
                stack.add(candidate);

                // Mark this box as used
                usedIds.add(candidate.getId());
            }
        }

        // Return the constructed initial stack
        return stack;
    }

    /**
     * Generates a neighbor solution by modifying the current stack of boxes.
     * 
     * @param currentStack    The current stack of boxes.
     * @param allOrientations The list of all box orientations.
     * @param temperature     The current temperature for the annealing process.
     * @return A new stack of boxes representing a neighbor solution.
     */
    private List<Box> getNeighborSolution(List<Box> currentStack, List<Box> allOrientations, int temperature) {

        // create a random number generator
        Random random = new Random();

        // create a new stack (list) based on the current stack
        List<Box> newStack = new ArrayList<>(currentStack);

        // create a set to keep track of used box IDs
        Set<Integer> usedIds = new HashSet<>();

        // loop through each box in the new stack
        for (Box b : newStack)

            // add the box ID to the used IDs set
            usedIds.add(b.getId());

        // iterate temperature amount of times
        for (int t = 0; t < temperature; t++) {

            // set action to a random integer between 0 and 2
            int action = random.nextInt(3);

            // check if action is 0 (insert)
            if (action == 0) {

                // create a list to hold candidates for insertion
                List<Box> candidates = new ArrayList<>();

                // loop through all orientations
                for (Box b : allOrientations) {

                    // check if the box is not already used
                    if (!usedIds.contains(b.getId()))

                        // add box to candidates
                        candidates.add(b);
                }

                // check if candidates list is not empty (available boxes to insert)
                if (!candidates.isEmpty()) {

                    // get a random box from candidates (to add to the stack)
                    Box toAdd = candidates.get(random.nextInt(candidates.size()));

                    // iterate through possible positions in the new stack
                    for (int pos = 0; pos <= newStack.size(); pos++) {

                        // check if the box can fit in the current position
                        boolean fits = (pos == 0 || canPlaceOnTop(newStack.get(pos - 1), toAdd)) &&
                                (pos == newStack.size() || canPlaceOnTop(toAdd, newStack.get(pos)));

                        // check if box fits
                        if (fits) {

                            // Insert the box at the position
                            newStack.add(pos, toAdd);

                            // add the box ID to used IDs
                            usedIds.add(toAdd.getId());

                            // break out of the loop (box inserted)
                            break;
                        }
                    }
                }

                // else if action is 1 (remove)
            } else if (action == 1 && !newStack.isEmpty()) {

                // get a random index from the new stack
                int idx = random.nextInt(newStack.size());

                // remove the id from used IDs
                usedIds.remove(newStack.get(idx).getId());

                // remove the box at the random index from the new stack
                newStack.remove(idx);

                // else check if action is 2 (substitute)
            } else if (action == 2 && !newStack.isEmpty()) {

                // get a random index from the new stack
                int idx = random.nextInt(newStack.size());

                // Get the ID of the box at the random index
                int boxId = newStack.get(idx).getId();

                // create a list to hold all orientations
                List<Box> orientations = new ArrayList<>();

                // loop through all orientations
                for (Box b : allOrientations) {

                    // check if the box ID match and is not equal to the box at the index
                    if (b.getId() == boxId && !b.equals(newStack.get(idx)))

                        // add the box to orientations
                        orientations.add(b);
                }

                // check if orientations list not empty
                if (!orientations.isEmpty()) {

                    // set new orientation to a random orientation from the list
                    Box newOrientation = orientations.get(random.nextInt(orientations.size()));

                    // check if the new orientation can fit in the stack
                    boolean fits = (idx == 0 || canPlaceOnTop(newStack.get(idx - 1), newOrientation)) &&
                            (idx == newStack.size() - 1 || canPlaceOnTop(newOrientation, newStack.get(idx + 1)));

                    // if new orientation fits
                    if (fits) {

                        // replace the box at the index with the new orientation
                        newStack.set(idx, newOrientation);
                    }
                }
            }
        }

        // initialise a list to hold the valid stack
        List<Box> validStack = new ArrayList<>();

        // clear the used IDs set
        usedIds.clear();

        // loop through each candidate box in the new stack
        for (Box candidate : newStack) {

            // check if the candidate box ID is already used
            if (usedIds.contains(candidate.getId()))

                // skip to the next candidate
                continue;

            // check if the valid stack is empty or if the candidate can be placed on top of
            // the last box in the valid stack
            if (validStack.isEmpty() || canPlaceOnTop(validStack.get(validStack.size() - 1), candidate)) {

                // add candidate to the valid stack
                validStack.add(candidate);

                // mark the candidate box ID as used
                usedIds.add(candidate.getId());
            }
        }

        // return the valid stack
        return validStack;
    }

    /**
     * Checks if a box can be placed on top of another box based on their
     * dimensions.
     * 
     * @param bottomBox The box at the bottom of the stack.
     * @param topBox    The box to be placed on top.
     * @return true if the top box can be placed on the bottom box, false otherwise.
     */

    private boolean canPlaceOnTop(Box bottomBox, Box topBox) {

        // initalise int array to hold below dimensions
        int[] below = bottomBox.getDimensions();

        // initalise int array to hold above dimensions
        int[] above = topBox.getDimensions();

        // return true if the top box's dimensions are less than the bottom box's
        // dimensions
        return (above[0] < below[0] && above[1] < below[1]);
    }

    /**
     * Calculates the total height of a stack of boxes.
     * 
     * @param stack The list of boxes in the stack.
     * @return The total height of the stack.
     */
    private int getStackHeight(List<Box> stack) {

        // initialise height to 0
        int height = 0;

        // loop through each box in the stack
        for (Box box : stack) {

            // add the height of the box to the total height
            height += box.getDimensions()[2];
        }

        // return the total height of the stack
        return height;
    }

    /**
     * Prints the dimensions of each box in the stack along with the cumulative
     * height.
     * 
     * @param stack The list of boxes in the stack.
     */
    private void printStack(List<Box> stack) {

        // initialise total height to 0
        int totalHeight = 0;

        // loop through each box in the stack in reverse order
        for (int i = stack.size() - 1; i >= 0; i--) {

            // get the box at index i
            Box box = stack.get(i);

            // get the dimensions of the box
            int[] dimensions = box.getDimensions();

            // add the height of the box to the total height
            totalHeight += dimensions[2];

            // print the dimensions of the box and the total height so far
            System.out.println(dimensions[0] + " " + dimensions[1] + " " + dimensions[2] + " " + totalHeight);
        }
    }
}

/**
 * Nested class representing a box with its dimensions and unique ID.
 * Each box can have three unique orientations based on its dimensions.
 * The dimensions are stored in the order [width, length, height].
 * The class provides methods to generate all orientations of a box.
 */
class Box {

    // private field for dimensions (width, length, height)
    private final int[] dimensions;

    // private field for unique id
    private final int id;

    /**
     * Constructor to create a Box object with given dimensions and ID.
     * 
     * @param dimensions An array of integers representing the dimensions of the box
     *                   in the order [width, length, height].
     * @param id         A unique identifier for the box.
     */
    public Box(int[] dimensions, int id) {

        // set dimensions
        this.dimensions = dimensions;

        // set id
        this.id = id;
    }

    /**
     * Getter method to retrieve the dimensions of the box.
     * 
     * @return An array of integers representing the dimensions of the box.
     */
    public int[] getDimensions() {

        // return dimensions (as an array)
        return dimensions;
    }

    /**
     * Getter method to retrieve the unique ID of the box.
     * 
     * @return The unique identifier of the box.
     */
    public int getId() {

        // return id
        return id;
    }

    /**
     * Generates all three orientations of a box based on its dimensions.
     * Each orientation is represented as a Box object with the same ID.
     * 
     * @param dims The dimensions of the box in the order [width, length, height].
     * @param id   The unique identifier for the box.
     * @return A list of Box objects representing all orientations of the box.
     */
    public static List<Box> generateOrientations(int[] dims, int id) {

        // Create a list to hold all orientations of the box
        List<Box> orientations = new ArrayList<>();

        // Add all three orientations of the box to the list
        orientations.add(new Box(new int[] { dims[0], dims[1], dims[2] }, id));
        orientations.add(new Box(new int[] { dims[1], dims[2], dims[0] }, id));
        orientations.add(new Box(new int[] { dims[2], dims[0], dims[1] }, id));

        // Return the list of orientations
        return orientations;
    }
}
