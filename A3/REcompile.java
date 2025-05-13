// Name: Hiran Greening
// ID: 1522172
// Last Modified: 2025-05-12
// Resources Used: COMPX301 lecture notes (Week 7 & 8)

import java.util.ArrayList;
import java.util.List;

public class REcompile {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java REcompile \"regex_pattern\"");
            return;
        }

        String regex = args[0];
        if (regex.isEmpty()) {
            System.out.println("Error: Regex pattern cannot be empty.");
            return;
        }

        try {
            FSMCompiler compiler = new FSMCompiler(regex);
            List<FSMCompiler.State> fsm = compiler.compile();
            printFSM(fsm);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printFSM(List<FSMCompiler.State> fsm) {
        for (FSMCompiler.State state : fsm) {
            System.out.printf("%d,%s,%d,%d%n",
                state.stateNum, state.symbol, state.next1, state.next2);
        }
    }
}

class FSMCompiler {
    private final String regex;
    private int pos = 0;
    private int stateCounter = 0;
    private final List<State> fsm = new ArrayList<>();

    static class State {
        final int stateNum;
        final String symbol;
        int next1;
        int next2;

        State(int stateNum, String symbol, int next1, int next2) {
            this.stateNum = stateNum;
            this.symbol = symbol;
            this.next1 = next1;
            this.next2 = next2;
        }
    }

    public FSMCompiler(String regex) {
        this.regex = regex;
    }

    public List<State> compile() throws Exception {
        int initial = newState("BR", -1, -1);
        int startState = parseExpression();
        int finalState = newState("BR", -1, -1);

        State s0 = getState(initial);
        s0.next1 = startState;
        s0.next2 = startState;

        // Patch all non-branch states with next1 == -1 to finalState
        for (State s : fsm) {
            if (!s.symbol.equals("BR") && s.next1 == -1) {
                s.next1 = finalState;
                s.next2 = finalState;
            }
        }

        // Patch all branch states (except initial and final) with next1 == -1 to finalState
        for (State s : fsm) {
            if (s.symbol.equals("BR") && s.stateNum != initial && s.stateNum != finalState && s.next1 == -1) {
                s.next1 = finalState;
                s.next2 = finalState;
            }
        }

        return fsm;
    }

    private int parseExpression() throws Exception {
        if (hasMore() && peek() == '|') {
            throw new Exception("Unexpected | at start of expression");
        }
        int left = parseTerm();
        if (hasMore() && peek() == '|') {
            consume();

            // --- FIX: Create branch state BEFORE alternatives ---
            int branch = newBranchState(-1, -1); // Reserve the branch state number

            int leftStart = left;
            int rightStart = parseExpression();

            // Set the branch state's transitions to the alternatives
            State branchState = getState(branch);
            branchState.next1 = leftStart;
            branchState.next2 = rightStart;

            // Create join state
            int join = newState("BR", -1, -1);

            // Patch left and right alternatives to point to join
            patchToJoin(leftStart, join);
            patchToJoin(rightStart, join);

            return branch;
        }
        return left;
    }

    // Helper to patch all end states of a branch to the join state
    private void patchToJoin(int stateNum, int join) {
        State s = getState(stateNum);
        if (!s.symbol.equals("BR") && s.next1 == -1) {
            s.next1 = join;
            s.next2 = join;
        } else if (s.symbol.equals("BR")) {
            if (s.next1 != -1) patchToJoin(s.next1, join);
            if (s.next2 != -1 && s.next2 != s.next1) patchToJoin(s.next2, join);
        }
    }

    private int parseTerm() throws Exception {
        return parseFactor();
    }

    private int parseFactor() throws Exception {
        int atom = parseAtom();
        
        if (hasMore() && isRepetition(peek())) {
            char op = consume();
            switch (op) {
                case '*': return handleStar(atom);
                case '+': return handlePlus(atom);
                case '?': return handleOptional(atom);
            }
        }
        return atom;
    }

    private int parseAtom() throws Exception {
        if (peek() == '(') {
            consume();
            int expr = parseExpression();
            if (peek() != ')') throw new Exception("Unmatched parentheses");
            consume();
            return expr;
        } else if (peek() == '.') {
            consume();
            int wcState = newState("WC", -1, -1);
            return wcState;
        } else if (peek() == '\\') {
            consume();
            if (!hasMore()) throw new Exception("Trailing backslash");
            char escapedChar = consume();
            int charState = newState(String.valueOf(escapedChar), -1, -1);
            return charState;
        } else {
            char c = consume();
            if (isSpecial(c)) throw new Exception("Unescaped special character: " + c);
            int charState = newState(String.valueOf(c), -1, -1);
            return charState;
        }
    }

    private int handleStar(int atom) {
        int branch = newBranchState(atom, stateCounter);
        linkStates(getLastState(atom), branch);
        return branch;
    }

    private int handlePlus(int atom) {
        linkStates(getLastState(atom), atom);
        return atom;
    }

    private int handleOptional(int atom) {
        return newBranchState(atom, stateCounter);
    }

    private int newState(String symbol, int next1, int next2) {
        State s = new State(stateCounter++, symbol, next1, next2);
        fsm.add(s);
        return s.stateNum;
    }

    private int newBranchState(int next1, int next2) {
        return newState("BR", next1, next2);
    }

    private void linkStates(int from, int to) {
        State s = getState(from);
        if (s.next1 == -1) s.next1 = to;
        else if (s.next2 == -1) s.next2 = to;
    }

    private int getLastState(int start) {
        State s = getState(start);
        if (s.next1 == start || s.next2 == start) return start; // For loops
        if (s.next1 == -1 && s.next2 == -1) return start;
        if (s.next1 != -1) return getLastState(s.next1);
        return getLastState(s.next2);
    }

    private State getState(int stateNum) {
        for (State s : fsm) {
            if (s.stateNum == stateNum) return s;
        }
        throw new RuntimeException("State not found: " + stateNum);
    }

    private char peek() {
        return pos < regex.length() ? regex.charAt(pos) : '\0';
    }

    private char consume() {
        return regex.charAt(pos++);
    }

    private boolean hasMore() {
        return pos < regex.length();
    }

    private boolean isRepetition(char c) {
        return c == '*' || c == '+' || c == '?';
    }

    private boolean isSpecial(char c) {
        return "*+?|().\\".indexOf(c) != -1;
    }
}