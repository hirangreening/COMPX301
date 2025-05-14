// Name: Hiran Greening
// ID: 1522172

// Import Statements
import java.util.ArrayList;
import java.util.List;

/**
 * REcompile.java
 * A simple regex compiler that converts a regex pattern into a finite state
 * machine (FSM).
 * The FSM is represented as a list of states, each with its transitions.
 * The program takes a regex pattern as input and outputs the FSM in a specific
 * format.
 */
public class REcompile {

    /**
     * Main method to run the REcompile program.
     * It takes a regex pattern as a command-line argument and compiles it into an
     * FSM.
     *
     * @param args Command-line arguments, expecting a single regex pattern.
     */
    public static void main(String[] args) {

        // check if arguments provided are valid
        if (args.length != 1) {

            // print usage message
            System.out.println("Usage: java REcompile \"regex_pattern\"");

            // exit program
            return;
        }

        // get regex from arguments
        String regex = args[0];

        // check if regex is empty
        if (regex.isEmpty()) {

            // print error message
            System.out.println("Error: Regex pattern cannot be empty.");

            // exit program
            return;
        }

        // try-catch block
        try {

            // initialize FSMCompiler with regex
            FSMCompiler compiler = new FSMCompiler(regex);

            // Compile the regex into a finite state machine (FSM)
            List<FSMCompiler.State> fsm = compiler.compile();

            // method call to print the FSM
            printFSM(fsm);

            // catch exceptions
        } catch (Exception e) {

            // print error message
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Prints the FSM in a specific format.
     * Each state is printed with its state number, symbol, and transitions.
     *
     * @param fsm The list of states representing the FSM.
     */
    private static void printFSM(List<FSMCompiler.State> fsm) {

        // for each state in the FSM
        for (FSMCompiler.State state : fsm) {

            // print the state number, symbol, and transitions
            System.out.printf("%d,%s,%d,%d%n",
                    state.stateNum, state.symbol, state.next1, state.next2);
        }
    }
}

/**
 * FSMCompiler.java
 * A class that compiles a regex pattern into a finite state machine (FSM).
 * The FSM is represented as a list of states, each with its transitions.
 */
class FSMCompiler {

    // regex pattern to compile
    private final String regex;

    // current (parsing) position in the regex string
    private int pos = 0;

    // counter for assigning (unique) state numbers
    private int stateCounter = 0;

    // list of states representing the FSM
    private final List<State> fsm = new ArrayList<>();

    /**
     * State class representing a state in the FSM.
     * Each state has a state number, a symbol, and two transitions (next1 and
     * next2).
     */
    static class State {

        // Identifier for the state
        final int stateNum;

        // Character or symbol linked to this state
        final String symbol;

        // Transition state numbers
        int next1;
        int next2;

        /**
         * Constructor for the State class.
         *
         * @param stateNum The state number.
         * @param symbol   The symbol associated with the state.
         * @param next1    The first transition state number.
         * @param next2    The second transition state number.
         */
        State(int stateNum, String symbol, int next1, int next2) {

            // initialise with parameters
            this.stateNum = stateNum;
            this.symbol = symbol;
            this.next1 = next1;
            this.next2 = next2;
        }
    }

    /**
     * Constructor for the FSMCompiler class.
     *
     * @param regex The regex pattern to compile.
     */
    public FSMCompiler(String regex) {

        // check if regex is null or empty
        if (regex == null || regex.isEmpty()) {

            // throw an exception
            throw new IllegalArgumentException("Regex cannot be null or empty");
        }

        // initalise regex field with provided value
        this.regex = regex;
    }

    /**
     * Compiles the regex pattern into a finite state machine (FSM).
     * Creates an initial state, parses the regex to generate states, and links
     * them.
     * Ensures all states are connected to a final state.
     *
     * @return A list of states representing the FSM.
     * @throws Exception If an error occurs during parsing or state creation.
     */
    public List<State> compile() throws Exception {

        // Create the initial state (branch state with no transitions)
        int initial = newState("BR", -1, -1);

        // Parse the regex expression to determine the start state
        int startState = parseExpression();

        // Create the final state (branch state with no transitions)
        int finalState = newState("BR", -1, -1);

        // Retrieve the initial state object
        State s0 = getState(initial);

        // Set both transitions of the initial state to the start state
        s0.next1 = startState;
        s0.next2 = startState;

        // Iterate through all states in the FSM
        for (State s : fsm) {

            // If the state is not a branch and has no first transition
            if (!s.symbol.equals("BR") && s.next1 == -1) {

                // Set both transitions to the final state
                s.next1 = finalState;
                s.next2 = finalState;

                // If the state is not a branch and has no second transition
            } else if (!s.symbol.equals("BR") && s.next2 == -1) {

                // Set the second transition to the same as the first
                s.next2 = s.next1;
            }
        }

        // Iterate through all states again to handle branch states
        for (State s : fsm) {

            // If the state is a branch and not the initial or final state
            if (s.symbol.equals("BR") && s.stateNum != initial && s.stateNum != finalState && s.next1 == -1) {

                // Set both transitions to the final state
                s.next1 = finalState;
                s.next2 = finalState;
            }
        }

        // Return the compiled FSM as a list of states
        return fsm;
    }

    /**
     * Parses a regex expression and constructs the corresponding FSM states.
     * Handles alternation (|) by creating branch states and linking alternatives.
     * Ensures proper error handling for unexpected or misplaced characters.
     *
     * @return The state number of the parsed expression.
     * @throws Exception If there is an error during parsing, such as unexpected or
     *                   unmatched characters.
     */
    private int parseExpression() throws Exception {

        // Check if the expression starts with an unexpected '|'
        if (hasMore() && peek() == '|') {

            // Throw an exception for invalid syntax
            throw new Exception("Unexpected | at start of expression");
        }

        // Parse the left-hand side of the expression as a term
        int left = parseTerm();

        // Check if there is an alternation operator ('|')
        if (hasMore() && peek() == '|') {

            // Consume the '|' character to process the alternation
            consume();

            // Create a branch state with no transitions initially
            int branch = newBranchState(-1, -1);

            // Store the starting state of the left-hand side
            int leftStart = left;

            // Parse the right-hand side of the expression recursively
            int rightStart = parseExpression();

            // Retrieve the branch state object
            State branchState = getState(branch);

            // Set the first transition of the branch state to the left alternative
            branchState.next1 = leftStart;

            // Set the second transition of the branch state to the right alternative
            branchState.next2 = rightStart;

            // Create a join state to merge the two alternatives
            int join = newState("BR", -1, -1);

            // Patch the left and right alternatives to the join state
            patchToJoin(leftStart, join);
            patchToJoin(rightStart, join);

            // Return the branch state as the entry point for the alternation
            return branch;
        }

        // Return the state number of the left-hand side if no alternation is present
        return left;
    }

    /**
     * Patches the FSM to connect a specified state to a given join state.
     * Ensures that all outgoing transitions from the specified state lead to the
     * join state.
     * Handles both branch and non-branch states recursively.
     *
     * @param stateNum The state number to patch.
     * @param join     The join state number.
     */
    private void patchToJoin(int stateNum, int join) {

        // Retrieve the state object for the given state number
        State s = getState(stateNum);

        // If the state is not a branch and has no outgoing transitions
        if (!s.symbol.equals("BR") && s.next1 == -1) {

            // Set both transitions to point to the join state
            s.next1 = join;
            s.next2 = join;

            // If the state is a branch state
        } else if (s.symbol.equals("BR")) {

            // Check if the first transition is set
            if (s.next1 != -1) {

                // Recursively patch the first transition to the join state
                patchToJoin(s.next1, join);
            }

            // Check if the second transition is set and is different from the first
            if (s.next2 != -1 && s.next2 != s.next1) {

                // Recursively patch the second transition to the join state
                patchToJoin(s.next2, join);
            }
        }
    }

    /**
     * Parses a term in the regex expression and links its factors.
     *
     * @return The state number of the first state in the term.
     * @throws Exception If parsing fails.
     */
    private int parseTerm() throws Exception {

        // Parse the first factor in the term and set it as the first state
        int first = parseFactor();

        // Initialize the last state to the first state
        int last = first;

        // While there are more characters and the next character is not '|' or ')'
        while (hasMore() && peek() != '|' && peek() != ')') {

            // Parse the next factor in the term
            int next = parseFactor();

            // Link the last state to the next state
            linkStates(getLastState(last), next);

            // Update the last state to the next state
            last = next;
        }

        // Return the first state of the term
        return first;
    }

    /**
     * Parses a factor in the regex expression.
     *
     * @return The state number of the parsed factor.
     * @throws Exception If there is an error during parsing.
     */
    private int parseFactor() throws Exception {

        // Parse the next atom (a character, group, or wildcard)
        int atom = parseAtom();

        // Check if there are more characters and the next character is a repetition
        // operator
        if (hasMore() && isRepetition(peek())) {

            // Consume the repetition operator (*, +, or ?)
            char op = consume();

            // Handle the repetition operator accordingly
            switch (op) {
                case '*':
                    // Handle zero or more repetitions (Kleene star)
                    return handleStar(atom);
                case '+':
                    // Handle one or more repetitions (Kleene plus)
                    return handlePlus(atom);
                case '?':
                    // Handle zero or one occurrence (optional)
                    return handleOptional(atom);
            }
        }

        // If there is no repetition operator, return the atom as is
        return atom;
    }

    /**
     * Parses an atom in the regex expression.
     * An atom can be a character, group, wildcard, or escaped character.
     *
     * @return The state number of the parsed atom.
     * @throws Exception If parsing fails, such as unmatched parentheses or invalid
     *                   escape sequences.
     */
    private int parseAtom() throws Exception {

        // Check if the next character is an opening parenthesis
        if (peek() == '(') {

            // Consume the opening parenthesis
            consume();

            // Parse the expression inside the parentheses
            int expr = parseExpression();

            // Ensure the next character is a closing parenthesis
            if (peek() != ')') {

                // Throw an exception for unmatched parentheses
                throw new Exception("Unmatched parentheses");
            }

            // Consume the closing parenthesis
            consume();

            // Return the state number of the parsed expression
            return expr;

            // Check if the next character is a period (wildcard)
        } else if (peek() == '.') {

            // Consume the period character
            consume();

            // Create and return a wildcard state
            return newState("WC", -1, -1);

            // Check if the next character is a backslash (escape sequence)
        } else if (peek() == '\\') {

            // Consume the backslash character
            consume();

            // Ensure there are more characters to parse
            if (!hasMore()) {

                // Throw an exception for a trailing backslash
                throw new Exception("Trailing backslash");
            }

            // Consume the escaped character
            char escapedChar = consume();

            // Create and return a state for the escaped character
            return newState(String.valueOf(escapedChar), -1, -1);

            // Handle a regular character
        } else {

            // Consume the character
            char c = consume();

            // Ensure the character is not a special regex character
            if (isSpecial(c)) {

                // Throw an exception for unescaped special characters
                throw new Exception("Unescaped special character: " + c);
            }

            // Create and return a state for the character
            return newState(String.valueOf(c), -1, -1);
        }
    }

    /**
     * Handles the Kleene star (*) repetition operator.
     * Creates a branch state that loops back to the atom and links to the next
     * state.
     *
     * @param atom The state number of the atom to apply the star operator to.
     * @return The state number of the new branch state.
     */
    private int handleStar(int atom) {

        // Create a branch state with the atom as the first transition
        int branch = newBranchState(atom, stateCounter);

        // Link the last state of the atom back to the branch state
        linkStates(getLastState(atom), branch);

        // Return the branch state
        return branch;
    }

    /**
     * Handles the Kleene plus (+) operator.
     * Ensures at least one occurrence of the atom by creating a loop.
     *
     * @param atom The state number of the atom to loop.
     * @return The starting state of the atom.
     */
    private int handlePlus(int atom) {

        // Create a loop by linking the last state back to the atom
        linkStates(getLastState(atom), atom);

        // Return the starting state
        return atom;
    }

    /**
     * Handles the '?' operator (optional).
     * Creates a branch state to the atom or next state.
     *
     * @param atom State number of the atom.
     * @return State number of the new branch state.
     */
    private int handleOptional(int atom) {

        // Create a branch state to the atom or next state
        return newBranchState(atom, stateCounter);
    }

    /**
     * Creates a new state and adds it to the FSM.
     *
     * @param symbol The symbol for the state (literal, "BR", or "WC").
     * @param next1  The first transition state number.
     * @param next2  The second transition state number.
     * @return The state number of the new state.
     */
    private int newState(String symbol, int next1, int next2) {

        // Create a new state object with the given parameters
        State s = new State(stateCounter++, symbol, next1, next2);

        // Add the new state to the FSM list
        fsm.add(s);

        // Return the state number of the new state
        return s.stateNum;
    }

    /**
     * Creates a new branch state ("BR") with the given transitions.
     *
     * @param next1 The first transition state number.
     * @param next2 The second transition state number.
     * @return The state number of the new branch state.
     */
    private int newBranchState(int next1, int next2) {

        // Create and return a new branch state
        return newState("BR", next1, next2);
    }

    /**
     * Links a state to another state by setting its next1 or next2 transition.
     *
     * @param from The state number to link from.
     * @param to   The state number to link to.
     */
    private void linkStates(int from, int to) {

        // Get the state object for the 'from' state
        State s = getState(from);

        // If next1 is not set
        if (s.next1 == -1)

            // set it to 'to'
            s.next1 = to;

        // Otherwise, if next2 is not set
        else if (s.next2 == -1)

            // set it to 'to'
            s.next2 = to;
    }

    /**
     * Gets the last state in a chain starting from the given state.
     * Recursively traverses transitions to find the final state.
     *
     * @param start The starting state number.
     * @return The state number of the last state in the chain.
     */
    private int getLastState(int start) {

        // Retrieve the state object for the given state number
        State s = getState(start);

        // Check if the state loops to itself
        if (s.next1 == start || s.next2 == start)

            // Return the state number if it loops to itself
            return start;

        // Check if the state has no transitions
        if (s.next1 == -1 && s.next2 == -1)

            // Return the state number if it has no transitions
            return start;

        // Check if the first transition is set
        if (s.next1 != -1)

            // Recursively follow the first transition
            return getLastState(s.next1);

        // Recursively follow the second transition
        return getLastState(s.next2);
    }

    /**
     * Retrieves the State object for a given state number.
     *
     * @param stateNum The state number to retrieve.
     * @return The State object with the given state number.
     */
    private State getState(int stateNum) {

        // Iterate through all states in the FSM
        for (State s : fsm) {

            // check if the state number matches
            if (s.stateNum == stateNum)

                // return the state
                return s;
        }

        // If not found, throw an exception
        throw new RuntimeException("State not found: " + stateNum);
    }

    /**
     * Peeks at the next character in the regex without consuming it.
     *
     * @return The next character, or '\0' if at the end.
     */
    private char peek() {

        // Return the next character if available, otherwise '\0'
        return pos < regex.length() ? regex.charAt(pos) : '\0';
    }

    /**
     * Consumes and returns the next character in the regex.
     *
     * @return The next character.
     */
    private char consume() {

        // Return the current character and advance the position
        return regex.charAt(pos++);
    }

    /**
     * Checks if there are more characters to parse in the regex.
     *
     * @return True if more characters, false otherwise.
     */
    private boolean hasMore() {

        // Return true if the current position is less than the regex length
        return pos < regex.length();
    }

    /**
     * Checks if a character is a repetition operator (*, +, or ?).
     *
     * @param c The character to check.
     * @return True if the character is a repetition operator, false otherwise.
     */
    private boolean isRepetition(char c) {

        // Return true if the character is *, +, or ?
        return c == '*' || c == '+' || c == '?';
    }

    /**
     * Checks if a character is a special regex character.
     *
     * @param c The character to check.
     * @return True if the character is special, false otherwise.
     */
    private boolean isSpecial(char c) {

        // Return true if the character is a special regex character
        return "*+?|().\\".indexOf(c) != -1;
    }
}