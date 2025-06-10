import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NPCStack {

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: java NPCStack <input_file> <initial_temperature> <cooling_rate> [runs]");
            return;
        }

        String filename = args[0];
        int initialTemperature = Integer.parseInt(args[1]);
        double coolingRate = Double.parseDouble(args[2]);
        int runs = (args.length == 4) ? Integer.parseInt(args[3]) : 1;

        NPCStack npcStack = new NPCStack();
        List<Box> boxes = npcStack.readInputFile(filename);

        List<Box> bestStack = null;
        int bestHeight = 0;
        for (int i = 0; i < runs; i++) {
            List<Box> candidate = npcStack.simulatedAnnealing(boxes, initialTemperature, coolingRate);
            int height = npcStack.getStackHeight(candidate);
            if (bestStack == null || height > bestHeight) {
                bestStack = candidate;
                bestHeight = height;
            }
        }
        npcStack.printStack(bestStack);
    }

    public List<Box> readInputFile(String filename) {
        List<Box> allOrientations = new ArrayList<>();
        int boxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3) {
                    try {
                        int[] dimensions = new int[3];
                        for (int i = 0; i < 3; i++) {
                            dimensions[i] = Integer.parseInt(parts[i]);
                            if (dimensions[i] <= 0) {
                                throw new NumberFormatException();
                            }
                        }
                        allOrientations.addAll(Box.generateOrientations(dimensions, boxId));
                        boxId++;
                    } catch (NumberFormatException e) {
                        // Ignore lines with invalid dimensions
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allOrientations;
    }

    public List<Box> simulatedAnnealing(List<Box> boxes, int initialTemperature, double coolingRate) {
        List<Box> currentSolution = generateInitialSolution(boxes);
        List<Box> bestSolution = new ArrayList<>(currentSolution);

        double temperature = initialTemperature;

        while (temperature > 1) {
            List<Box> newSolution = getNeighborSolution(currentSolution, boxes, (int) temperature);

            if (getStackHeight(newSolution) > getStackHeight(currentSolution)) {
                currentSolution = newSolution;

                if (getStackHeight(currentSolution) > getStackHeight(bestSolution)) {
                    bestSolution = new ArrayList<>(currentSolution);
                }
            } else {
                double acceptanceProbability = Math.exp((getStackHeight(currentSolution) - getStackHeight(newSolution)) / temperature);
                if (Math.random() < acceptanceProbability) {
                    currentSolution = newSolution;
                }
            }

            temperature -= coolingRate;
        }

        return bestSolution;
    }

    private List<Box> generateInitialSolution(List<Box> allOrientations) {
        // Greedy: try to build a valid stack using tallest boxes/orientations first, one per box id
        List<Box> sorted = new ArrayList<>(allOrientations);
        sorted.sort((a, b) -> Integer.compare(b.getDimensions()[2], a.getDimensions()[2]));
        List<Box> stack = new ArrayList<>();
        List<Integer> usedIds = new ArrayList<>();
        for (Box candidate : sorted) {
            if (usedIds.contains(candidate.getId())) continue;
            if (stack.isEmpty() || canPlaceOnTop(stack.get(stack.size() - 1), candidate)) {
                stack.add(candidate);
                usedIds.add(candidate.getId());
            }
        }
        return stack;
    }

    private List<Box> getNeighborSolution(List<Box> currentStack, List<Box> allOrientations, int temperature) {
        // Try to change up to 'temperature' boxes in the stack
        Random random = new Random();
        List<Box> newStack = new ArrayList<>(currentStack);
        List<Integer> usedIds = new ArrayList<>();
        for (Box b : newStack) usedIds.add(b.getId());
        for (int t = 0; t < temperature; t++) {
            int action = random.nextInt(3); // 0: insert, 1: remove, 2: substitute
            if (action == 0) { // insert
                // Find a box not used yet
                List<Box> candidates = new ArrayList<>();
                for (Box b : allOrientations) {
                    if (!usedIds.contains(b.getId())) candidates.add(b);
                }
                if (!candidates.isEmpty()) {
                    Box toAdd = candidates.get(random.nextInt(candidates.size()));
                    // Try to insert at a random position where it fits
                    for (int pos = 0; pos <= newStack.size(); pos++) {
                        boolean fits = (pos == 0 || canPlaceOnTop(newStack.get(pos - 1), toAdd)) &&
                                       (pos == newStack.size() || canPlaceOnTop(toAdd, newStack.get(pos)));
                        if (fits) {
                            newStack.add(pos, toAdd);
                            usedIds.add(toAdd.getId());
                            break;
                        }
                    }
                }
            } else if (action == 1 && !newStack.isEmpty()) { // remove
                int idx = random.nextInt(newStack.size());
                usedIds.remove((Integer)newStack.get(idx).getId());
                newStack.remove(idx);
            } else if (action == 2 && !newStack.isEmpty()) { // substitute
                int idx = random.nextInt(newStack.size());
                int boxId = newStack.get(idx).getId();
                // Find a different orientation for this box
                List<Box> orientations = new ArrayList<>();
                for (Box b : allOrientations) {
                    if (b.getId() == boxId && !b.equals(newStack.get(idx))) orientations.add(b);
                }
                if (!orientations.isEmpty()) {
                    Box newOrientation = orientations.get(random.nextInt(orientations.size()));
                    // Check fit
                    boolean fits = (idx == 0 || canPlaceOnTop(newStack.get(idx - 1), newOrientation)) &&
                                   (idx == newStack.size() - 1 || canPlaceOnTop(newOrientation, newStack.get(idx + 1)));
                    if (fits) {
                        newStack.set(idx, newOrientation);
                    }
                }
            }
        }
        // After changes, rebuild stack from bottom up to ensure validity
        List<Box> validStack = new ArrayList<>();
        usedIds.clear();
        for (Box candidate : newStack) {
            if (usedIds.contains(candidate.getId())) continue;
            if (validStack.isEmpty() || canPlaceOnTop(validStack.get(validStack.size() - 1), candidate)) {
                validStack.add(candidate);
                usedIds.add(candidate.getId());
            }
        }
        return validStack;
    }

    private boolean canPlaceOnTop(Box bottomBox, Box topBox) {
        int[] below = bottomBox.getDimensions();
        int[] above = topBox.getDimensions();
        // Touching faces: width and length of above must be strictly less than below
        return (above[0] < below[0] && above[1] < below[1]) || (above[1] < below[0] && above[0] < below[1]);
    }

    private int getStackHeight(List<Box> stack) {
        int height = 0;
        for (Box box : stack) {
            height += box.getDimensions()[2]; // Assuming the third dimension is height
        }
        return height;
    }

    private void printStack(List<Box> stack) {
        int totalHeight = 0;
        for (int i = stack.size() - 1; i >= 0; i--) {
            Box box = stack.get(i);
            int[] dimensions = box.getDimensions();
            totalHeight += dimensions[2];
            System.out.println(dimensions[0] + " " + dimensions[1] + " " + dimensions[2] + " " + totalHeight);
        }
    }
}

// Box class with orientation support
class Box {
    private final int[] dimensions; // [w, l, h] in this orientation
    private final int id; // unique id for the physical box

    public Box(int[] dimensions, int id) {
        this.dimensions = dimensions;
        this.id = id;
    }

    public int[] getDimensions() {
        return dimensions;
    }

    public int getId() {
        return id;
    }

    // Generate all 3 unique orientations for this box
    public static List<Box> generateOrientations(int[] dims, int id) {
        List<Box> orientations = new ArrayList<>();
        orientations.add(new Box(new int[]{dims[0], dims[1], dims[2]}, id));
        orientations.add(new Box(new int[]{dims[1], dims[2], dims[0]}, id));
        orientations.add(new Box(new int[]{dims[2], dims[0], dims[1]}, id));
        return orientations;
    }
}
