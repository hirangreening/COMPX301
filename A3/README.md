**Author**: Hiran Greening  
_**ID**_: 1522172  
**Partner**: Oleksandr Kashpir  
_**ID**_: 1637705  

# REcompile

---

## Compilation

This compiler is implemented as multiple classes within a single file, `REcompile.java`.  
To compile, use:
```
javac REcompile.java
```

---

## Usage

The program takes a regular expression as a command-line argument and outputs the corresponding FSM to standard output.  
To use it with the searcher, pipe the output into my partner's search program as follows:
```
java REcompile "regexp" | java REsearch filename.txt
```
Here, `"regexp"` is your regular expression (in quotes), and `filename.txt` is the file to search.

Alternatively, you can save the FSM output to a file and use:
```
cat FSMlines.txt | java REsearch filename.txt
```

---

## Comments

The Deque implementation in the searcher uses a persistent SCAN node, initialized with a value of -2.  
This is to avoid confusion with -1, which is reserved as the final state indicator (by agreement between Hiran and Oleksandr).
