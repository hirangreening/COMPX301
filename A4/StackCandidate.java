/*
 * StackCandidate.java
 * Represents a stack of boxes (with chosen orientations) and its total height.
 * Author: [Your Name], [Your Student ID]
 */

import java.util.*;

public class StackCandidate {
    public final List<Box> stack;
    public final int totalHeight;

    public StackCandidate(List<Box> stack) {
        this.stack = new ArrayList<>(stack);
        int h = 0;
        for (Box b : stack) h += b.h;
        this.totalHeight = h;
    }

    // Output as required: from top to bottom, with running height
    public void printStack() {
        int runningHeight = totalHeight;
        for (Box b : stack) {
            System.out.println(b.w + " " + b.l + " " + b.h + " " + runningHeight);
            runningHeight -= b.h;
        }
    }
}
