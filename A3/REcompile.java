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

    // private instance variables

    // The regex pattern to compile
    private final String regex;

    // current position in the regex string
    private int pos = 0;

    // counter for state numbers
    private int stateCounter = 0;

    // list to hold the states of the FSM
    private final List<State> fsm = new ArrayList<>();

    /**
     * State class representing a state in the FSM.
     * Each state has a state number, a symbol, and two transitions (next1 and
     * next2).
     */
    static class State {

        // private instance variables

        // The state number
        final int stateNum;

        // symbol associated with the state
        final String symbol;

        // next state numbers for transitions
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

        // initalise with passed regex
        this.regex = regex;
    }

    /**
     * Compiles the regex pattern into a finite state machine (FSM).
     *
     * @return A list of states representing the FSM.
     * @throws Exception If there is an error during compilation.
     */
    public List<State> compile() throws Exception {

        // set the initial state
        int initial = newState("BR", -1, -1);

        // set the start state by parsing the regex expression
        int startState = parseExpression();

        // set the final state (branch state with no transitions. -1
        int finalState = newState("BR", -1, -1);

        // initialise start state with the initial state
        State s0 = getState(initial);

        // set transitions with the start state
        s0.next1 = startState;
        s0.next2 = startState;

        // for each state in the FSM
        for (State s : fsm) {

            // check if the state is not the initial or final state
            if (!s.symbol.equals("BR") && s.next1 == -1) {

                // set the next states to the final state
                s.next1 = finalState;
                s.next2 = finalState;

                // If the state is not a branch and has no second transition
            } else if (!s.symbol.equals("BR") && s.next2 == -1) {

                // set the next state to the first transition
                s.next2 = s.next1;
            }
        }

        // for each state in the FSM
        for (State s : fsm) {

            // check if the state is a branch and not the initial or final state
            if (s.symbol.equals("BR") && s.stateNum != initial && s.stateNum != finalState && s.next1 == -1) {

                // set the next states to the final state
                s.next1 = finalState;
                s.next2 = finalState;
            }
        }

        // return the FSM
        return fsm;
    }

    /**
     * Parses the regex expression and builds the FSM.
     *
     * @return The state number of the parsed expression.
     * @throws Exception If there is an error during parsing.
     */
    private int parseExpression() throws Exception {
        if (hasMore() && peek() == '|') {
            throw new Exception("Unexpected | at start of expression");
        }

        int left = parseTerm();

        // If alternation, handle it
        if (hasMore() && peek() == '|') {
            consume(); // consume '|'

            // Create the branch state *before* parsing alternatives
            int branch = newBranchState(-1, -1);

            // Parse left and right alternatives
            int leftAlt = left;
            int rightAlt = parseExpression();

            // Set branch transitions
            getState(branch).next1 = leftAlt;
            getState(branch).next2 = rightAlt;

            // Create a join state for both alternatives to end at
            int join = newState("BR", -1, -1);

            // Patch both alternatives to the join state
            patchToJoin(leftAlt, join);
            patchToJoin(rightAlt, join);

            return branch;
        }

        return left;
    }

    /**
     * Patches the last state in the chain starting from stateNum to point to join.
     */
    private void patchToJoin(int stateNum, int join) {
        int last = getLastState(stateNum);
        State s = getState(last);
        s.next1 = join;
        s.next2 = join;
    }

    /**
     * Parses a term in the regex expression.
     *
     * @return The state number of the parsed term.
     * @throws Exception If there is an error during parsing.
     */
    private int parseTerm() throws Exception {

        // set first state with the parsed factor
        int first = parseFactor();

        // set last state with first state
        int last = first;

        // while there are more characters and the next character is not | or )
        while (hasMore() && peek() != '|' && peek() != ')') {

            // set next state with the parsed factor
            int next = parseFactor();

            // link the last state to the next state
            linkStates(getLastState(last), next);

            // set last state to the next state
            last = next;
        }

        // return the first state
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
     *
     * @return The state number of the parsed atom.
     * @throws Exception If there is an error during parsing.
     */
    private int parseAtom() throws Exception {

        // peek if the next character is opening parentheses
        if (peek() == '(') {

            // consume the opening parentheses
            consume();

            // parse the expression inside the parentheses
            int expr = parseExpression();

            // check if next character is not closing parentheses
            if (peek() != ')')

                // throw an exception
                throw new Exception("Unmatched parentheses");

            // consume the closing parentheses
            consume();

            // return expression state
            return expr;

            // check if next character is period (wildcard)
        } else if (peek() == '.') {

            // consume the period character
            consume();

            // create a wildcard state
            int wcState = newState("WC", -1, -1);

            // return wildcard state
            return wcState;

            // check if next character is double backslash
        } else if (peek() == '\\') {

            // consume the backslash character
            consume();

            // check if there are more characters
            if (!hasMore())

                // throw an exception
                throw new Exception("Trailing backslash");

            // consume escaped character
            char escapedChar = consume();

            // create a state for the escaped character
            int charState = newState(String.valueOf(escapedChar), -1, -1);

            // return character state
            return charState;
        } else {

            // consume the character
            char c = consume();

            // check if character is a special character
            if (isSpecial(c))

                // throw an exception
                throw new Exception("Unescaped special character: " + c);

            // create a state for the character
            int charState = newState(String.valueOf(c), -1, -1);

            // return character state
            return charState;
        }
    }

    /**
     * Handles the repetition operator '*' (Kleene star).
     *
     * @param atom The state number of the atom to apply the star operator to.
     * @return The state number of the new branch state created.
     */
    private int handleStar(int atom) {

        // create branch state with atom and state counter
        int branch = newBranchState(atom, stateCounter);

        // link the last state of the atom to the branch state
        linkStates(getLastState(atom), branch);

        // return the branch state
        return branch;
    }

    /**
     * Handles the repetition operator '+' (Kleene plus).
     *
     * @param atom The state number of the atom to apply the plus operator to.
     * @return The state number of the new branch state created.
     */
    private int handlePlus(int atom) {

        // Link the last state of the atom back to the atom itself to create a loop (one
        // or more repetitions)
        linkStates(getLastState(atom), atom);

        // Return the starting state of the atom as the entry point for the plus
        // operation
        return atom;
    }

    /**
     * Handles the repetition operator '?' (zero or one occurrence).
     *
     * @param atom The state number of the atom to apply the optional operator to.
     * @return The state number of the new branch state created.
     */
    private int handleOptional(int atom) {

        // Create a branch state that can go to the atom or skip to the next state
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

        // If next1 is not set, set it to 'to'
        if (s.next1 == -1)
            s.next1 = to;

        // Otherwise, if next2 is not set, set it to 'to'
        else if (s.next2 == -1)

            s.next2 = to;
    }

    /**
     * Recursively finds the last state in a chain starting from 'start'.
     *
     * @param start The starting state number.
     * @return The state number of the last state in the chain.
     */
    private int getLastState(int start) {

        // Get the state object for the starting state
        State s = getState(start);

        // If the state loops to itself, return it (for loops)
        if (s.next1 == start || s.next2 == start)
            return start;

        // If both transitions are unset, return this state
        if (s.next1 == -1 && s.next2 == -1)
            return start;

        // If next1 is set, continue recursively from next1
        if (s.next1 != -1)
            return getLastState(s.next1);

        // Otherwise, continue recursively from next2
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

            // If the state number matches, return the state
            if (s.stateNum == stateNum)
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
     * @return True if there are more characters, false otherwise.
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

        // Return true if the character is in the set of special characters
        return "*+?|().\\".indexOf(c) != -1;
    }
}