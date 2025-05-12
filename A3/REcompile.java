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
    private int stateCounter = 1;
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
        fsm.add(new State(0, "BR", -1, -1)); // Initial state
    }

    public List<State> compile() throws Exception {
        int startState = parseExpression();
        fsm.get(0).next1 = startState; // Set start state
        fsm.add(new State(stateCounter, "", -1, -1)); // Final state
        return fsm;
    }

    private int parseExpression() throws Exception {
        int left = parseTerm();
        
        if (hasMore() && peek() == '|') {
            consume();
            int right = parseExpression();
            return newBranchState(left, right);
        }
        return left;
    }

    private int parseTerm() throws Exception {
        int first = parseFactor();
        
        if (hasMore() && peek() != ')' && peek() != '|') {
            int second = parseTerm();
            linkStates(first, second);  // Link properly
        }        
        return first;
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
            return newState("WC", stateCounter, stateCounter + 1);  // Ensure correct transition
        } else if (peek() == '\\') {
            consume();  // Discard the backslash
            if (!hasMore()) throw new Exception("Trailing backslash");
    
            char escapedChar = consume();
            if (isSpecial(escapedChar)) {
                return newState(String.valueOf(escapedChar), stateCounter, stateCounter + 1);  // Treat as literal
            } else {
                return newState(String.valueOf(escapedChar), stateCounter, -1);  // Regular literal
            }
        } else {
            char c = consume();
            if (isSpecial(c)) throw new Exception("Unescaped special character: " + c);
            return newState(String.valueOf(c), stateCounter, -1);
        }
    }
    

    private int handleStar(int atom) {
        int branch = newBranchState(atom, stateCounter); // Create a branch state
        linkStates(getLastState(atom), branch); // Ensure exit transition
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
        int current = start;
        int counter = 0;  // Prevent infinite looping
        while (counter++ < fsm.size()) { 
            State s = getState(current);
            if (s.next1 == -1 && s.next2 == -1) return current;
            current = (s.next1 != -1) ? s.next1 : s.next2;
        }
        return start;  // Fallback to prevent infinite recursion
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