/*
 * Box.java
 * Represents a box and its possible orientations.
 * Author: [Your Name], [Your Student ID]
 */

import java.util.*;

public class Box {
    public final int w, l, h;
    public Box(int w, int l, int h) {
        this.w = w;
        this.l = l;
        this.h = h;
    }
    // Generate all 3 unique orientations for this box
    public List<Box> getOrientations() {
        Set<String> seen = new HashSet<>();
        List<Box> result = new ArrayList<>();
        int[][] perms = {
            {w, l, h}, {w, h, l}, {l, w, h}, {l, h, w}, {h, w, l}, {h, l, w}
        };
        for (int[] p : perms) {
            int[] base = {p[0], p[1], p[2]};
            Arrays.sort(base, 0, 2); // sort width and length for uniqueness
            String key = base[0] + "," + base[1] + "," + base[2];
            if (!seen.contains(key)) {
                result.add(new Box(p[0], p[1], p[2]));
                seen.add(key);
            }
        }
        return result;
    }
    // Check if this box can be placed on top of another
    public boolean fitsOn(Box below) {
        return (this.w < below.w && this.l < below.l) || (this.l < below.w && this.w < below.l);
    }
    @Override
    public String toString() {
        return w + " " + l + " " + h;
    }
}
