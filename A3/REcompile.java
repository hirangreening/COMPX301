// Name: Hiran Greening
// ID: 1522172

// Import Statements
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * It takes a regex pattern as a command-line argument, compiles it into an
     * FSM and prints the FSM to standard output.
     *
     * @param args Command-line arguments, expecting a single regex pattern.
     */
    public static void main(String[] args) {

        // check if arguments provided are valid
        if (args.length != 1) {

            // print usage message
            System.out.println(
                    "Usage: java REcompile \"regex_pattern\" (enclose the regex pattern in double quotes to handle special characters)");

            // exit program
            return;
        }

        // get regex from arguments
        String regex = args[0];

        // check if regex is empty
        if (regex.isEmpty()) {

            // print error message
            System.out.println("Error: Regex cannot be null or empty");

            // exit program
            return;
        }

        // try-catch block
        try {

            // initialise FSMCompiler with regex
            FSMCompiler compiler = new FSMCompiler(regex);

            // compile the regex into a finite state machine (FSM)
            List<FSMCompiler.State> fsm = compiler.compile();

            // method call to print the FSM
            printFSM(fsm);

            // catch exceptions
        } catch (Exception e) {

            // print error message
            System.out.println("Compilation error: " + e.getMessage());
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

            // print the state details: state number, symbol, first transition, second
            // transition
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

        // state number
        final int stateNum;

        // symbol associated with the state
        final String symbol;

        // first transition state number
        int next1;

        // second transition state number
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

        // validate regex input (must not be null or empty)
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

        // create an initial branch state with no transitions
        int initial = newState("BR", -1, -1);

        // set the start state by parsing the regex expression
        int startState = parseExpression();

        // set the final state (branch state with no transitions)
        int finalState = newState("BR", -1, -1);

        // retrieve the initial branch state object
        State s0 = getState(initial);

        // set transitions for the initial state to point to the start state
        s0.next1 = startState;
        s0.next2 = startState;

        // iterate through each state in the FSM
        for (State s : fsm) {

            // check if state symbol is not a branch state
            if (!s.symbol.equals("BR")) {

                // check if first transition is unset (-1)
                if (s.next1 == -1) {

                    // set first transition to the final state
                    s.next1 = finalState;
                }

                // check if second transition is unset (-1)
                if (s.next2 == -1) {

                    // set second transition to the final state
                    s.next2 = finalState;
                }
            }
        }

        // for each state in the FSM
        for (State s : fsm) {

            // check if this is a branch state with unset transitions (excluding
            // initial/final)
            if (s.symbol.equals("BR") && s.stateNum != initial && s.stateNum != finalState && s.next1 == -1) {

                // set both transitions to the final accepting state
                s.next1 = finalState;
                s.next2 = finalState;
            }
        }

        // return completed FSM
        return fsm;
    }

    /**
     * Parses the regex expression and builds the FSM.
     *
     * @return The state number of the parsed expression.
     * @throws Exception If there is an error during parsing.
     */
    private int parseExpression() throws Exception {

        // parse the first term in the expression
        int term = parseTerm();

        // check if the next character is the alternation ('|') operator
        if (hasMore() && peek() == '|') {

            // consume the alternation operator
            consume();

            // parse the right alternative
            int right = parseExpression();

            // create a branching state to merge both alternatives
            int join = newState("BR", -1, -1);

            // patch the end of both alternatives to the join state
            patchToJoin(term, join);
            patchToJoin(right, join);

            // create a branch state that splits to the left and right alternatives
            int branch = newBranchState(term, right);

            // return the branch state as the entry point for alternation ('|')
            return branch;
        }

        // If there is no alternation, return the term state
        return term;
    }

    /**
     * Recursively patches all states reachable from 'stateNum' where transitions
     * are unset (-1),
     * ensuring they converge to the 'join' state.
     *
     * @param stateNum The starting state number for patching.
     * @param join     The state number to redirect unset transitions to.
     */
    private void patchToJoin(int stateNum, int join) {

        // set to keep track of visited states
        Set<Integer> visited = new HashSet<>();

        // call the recursive helper method to patch the states
        patchToJoinHelper(stateNum, join, visited);
    }

    /**
     * Helper method to recursively patch states.
     *
     * @param stateNum The current state number being patched.
     * @param join     The state number to patch unset transitions to.
     * @param visited  A set to keep track of visited states to avoid cycles.
     */
    private void patchToJoinHelper(int stateNum, int join, Set<Integer> visited) {

        // check if stateNum is already visited
        if (!visited.add(stateNum)) {

            // exit method (avoid infinite loop)
            return;
        }

        // get the state object for the current state number
        State s = getState(stateNum);

        // check if state has no transitions
        if (s.next1 == -1) {

            // redirect first transition to the join state
            s.next1 = join;
        }

        // othwerwise recursively patch next reachable states
        else
            patchToJoinHelper(s.next1, join, visited);

        // check if state has no second transition
        if (s.next2 == -1) {

            // set second transition to the join state
            s.next2 = join;
        }

        // else if second transition is not the first
        else if (s.next2 != s.next1)

            // recursively patch the second transition
            patchToJoinHelper(s.next2, join, visited);
    }

    /**
     * Parses a sequence of factors (a "term") in the regex.
     * Handles concatenation by chaining factors together.
     *
     * @return The state number of the first factor in the term.
     * @throws Exception If there is an error during parsing.
     */
    private int parseTerm() throws Exception {

        // Parse the first factor in the term
        int first = parseFactor();

        // set last to the first factor
        int last = first;

        // Continue parsing and chaining factors until an alternation or closing
        // parenthesis is found
        while (hasMore() && peek() != '|' && peek() != ')') {

            // Parse the next factor in the term
            int next = parseFactor();

            // Create a branch state that connects the last state to the next state
            patchToJoin(last, next);

            // set the last state to the next state
            last = next;
        }

        // return the first state (entry point of the term)
        return first;
    }

    /**
     * Parses a factor in the regex expression.
     * A factor is an atom possibly followed by a repetition operator (*, +, ?).
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
                    return handleQuestion(atom);
            }
        }

        // If there is no repetition operator, return the atom as is
        return atom;
    }

    /**
     * Parses an atom in the regex expression.
     * An atom can be a grouped expression, a wildcard, an escaped character, or a
     * literal character.
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

            // check if next character is a period (wildcard)
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

            // check if there are no more characters
            if (!hasMore())

                // throw an exception
                throw new Exception("Trailing backslash");

            // consume escaped character
            char escapedChar = consume();

            // create a state for the escaped character
            int charState = newState(String.valueOf(escapedChar), -1, -1);

            // return character state
            return charState;

            // otherwise, treat the character as a literal
        } else {

            // consume the character
            char c = consume();

            // check if character is a special character
            if (isSpecial(c))

                // throw an exception
                throw new Exception("Unescaped special character: " + c);

            // create a state for the literal character
            int charState = newState(String.valueOf(c), -1, -1);

            // return literal character state
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

        // Create a branch that loops for zero or more repetitions
        int branch = newBranchState(atom, -1);

        // connect the last state of the atom to the branch
        linkStates(getLastState(atom), branch);

        // Return the branch state as the entry point for '*'
        return branch;
    }

    /**
     * Handles the repetition operator '+' (Kleene plus).
     *
     * @param atom The state number of the atom to apply the plus operator to.
     * @return The state number of the new branch state created.
     */
    private int handlePlus(int atom) {

        // ensure one occurence, then loop back to allow for more repetitions
        linkStates(getLastState(atom), atom);

        // return atom as the entry point for '+'
        return atom;
    }

    /**
     * Handles the repetition operator '?' (question mark).
     *
     * @param atom The state number of the atom to apply the question mark operator
     *             to.
     * @return The state number of the new branch state created.
     * @throws Exception If there is an error during handling.
     */
    private int handleQuestion(int atom) throws Exception {

        // create a join state for both branches to merge
        int join = newState("BR", -1, -1);

        // patch the end of the atom chain to the join state
        patchToJoin(atom, join);

        // create a branch that allows either skipping or including the atom
        int branch = newBranchState(atom, join);

        // return the branch state as the entry point for '?'
        return branch;
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

        // initialise state object with state number to link from
        State s = getState(from);

        // check if state is a branch state and has no second transition
        if (s.symbol.equals("BR") && s.next2 == -1 && s.next1 != -1) {
            // set second transition with state number to link to
            s.next2 = to;

            // else, check if state is not a branch state and has no first transition
        } else if (!s.symbol.equals("BR") && s.next1 == -1) {

            // set first transition with state number to link to
            s.next1 = to;
        }
    }

    /**
     * Recursively finds the last state in a chain starting from 'start'.
     *
     * @param start The starting state number.
     * @return The state number of the last state in the chain.
     */
    private int getLastState(int start) {

        // return the last state by calling the helper method
        return getLastStateHelper(start, new HashSet<>());
    }

    private int getLastStateHelper(int start, Set<Integer> visited) {

        // check if stateNum is already visited
        if (!visited.add(start)) {

            // return start (avoid infinite loop)
            return start;
        }

        // set state object with start state number
        State s = getState(start);

        // check if state has no outgoing transitions (end of path)
        if (s.next1 == -1 && s.next2 == -1)

            // return start (last reachable state)
            return start;

        // check if the first transition leads to another state
        if (s.next1 != -1 && s.next1 != start) {

            // recursively traverse the FSM along the first transition
            int last = getLastStateHelper(s.next1, visited);

            // check if recursion reached a deeper valid state
            if (last != s.next1) {

                // return the last reachable state found
                return last;
            }
        }

        // check if the second transition leads to another state
        if (s.next2 != -1 && s.next2 != start) {

            // recursively traverse the FSM along the second transition
            int last = getLastStateHelper(s.next2, visited);

            // check if recursion reached a deeper valid state
            if (last != s.next2) {

                // return the last reachable state found
                return last;
            }
        }

        // return start (no further valid transitions found)
        return start;
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

                // return the state object
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