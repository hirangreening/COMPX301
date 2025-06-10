#!/bin/bash
# filepath: /home/hiran/Documents/Trimester A 2025/COMPX301/A3/thorough_tests.sh

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Files
TESTFILE="test.txt"
MYOUT="my_output.txt"
GREPOUT="grep_output.txt"

# Test patterns (Thorough set)
declare -A patterns=(
  [T01]=""                       # Empty regex (edge case)
  [T02]="a"                      # Single literal
  [T03]="ab"                     # Concatenation
  [T04]="abc"                    # Longer concatenation
  [T05]="a*"                     # Zero or more
  [T06]="a+"                     # One or more
  [T07]="a?"                     # Zero or one
  [T08]="."                      # Wildcard
  [T09]=".."                     # Multiple wildcards
  [T10]="a.b"                    # Wildcard in concatenation
  [T11]=".*"                     # Zero or more wildcards
  [T12]=".+"                     # One or more wildcards
  [T13]="a|b"                    # Simple alternation
  [T14]="ab|cd"                  # Alternation of concatenations
  [T15]="a|bc|d"                # Multiple alternations
  [T16]="(a|b)c"                 # Grouping and concatenation
  [T17]="a(b|c)"                 # Concatenation and grouping
  [T18]="(ab)*"                  # Group with zero or more
  [T19]="(ab)+"                  # Group with one or more
  [T20]="(a|b)?"                 # Optional group
  [T21]="((a|b)c)|(de)"          # Nested groups and alternation
  [T22]="a\\*b"                   # Escaped asterisk (literal)
  [T23]="a\\+b"                   # Escaped plus (literal)
  [T24]="a\\?b"                   # Escaped question mark (literal)
  [T25]="a\\.b"                   # Escaped dot (literal)
  [T26]="a\\|b"                   # Escaped pipe (literal)
  [T27]="a\\\\b"                  # Escaped backslash (literal)
  [T28]="a\\(b\\)c"               # Escaped parentheses (literal)
  [T29]="(a*)*"                  # Nested closure (should be like a*)
  [T30]="(a+)+"                  # Nested plus (should be like a+)
  [T31]="(a?)?"                  # Nested optional (should be like a?)
  [T32]="a(b*c?)+d"             # More complex combination
  [T33]="(a|b)*c?"               # Another complex combination
  [T34]="a(b|c)*d"               # Closure on alternation in group
  [T35]="a(b|c)+d"               # Plus on alternation in group
  [T36]="a(b|c)?d"               # Optional alternation in group
)

# Initialize counters
passed=0
failed=0
total=0

# Arrays to track which tests passed/failed
passed_tests=()
failed_tests=()

echo "Compiling Java files..."
javac *.java

echo "===== RUNNING THOROUGH REGEX TESTS ====="
echo "Total tests: ${#patterns[@]}"
echo "======================================="

for key in $(printf "%s\n" "${!patterns[@]}" | sort); do
  regex="${patterns[$key]}"
  echo -e "\nTest $key: '$regex'"

  # Run your matcher
  java REcompile "$regex" | java REsearch "$TESTFILE" > "$MYOUT" 2>/dev/null

  # Run grep for reference
  grep -E "$regex" "$TESTFILE" > "$GREPOUT"

  # Compare outputs (ignore order, blank lines, trailing whitespace, and carriage returns)
  diff -u \
    <(sort "$MYOUT" | sed '/^$/d' | tr -d '\r' | sed 's/[[:space:]]*$//') \
    <(sort "$GREPOUT" | sed '/^$/d' | tr -d '\r' | sed 's/[[:space:]]*$//') > diff.txt

  if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}SUCCESS: Output matches grep${NC}"
    passed=$((passed + 1))
    passed_tests+=("$key")
  else
    echo -e "${RED}FAILURE: Output differs from grep${NC}"
    echo -e "${RED}--- Your output:${NC}"
    cat "$MYOUT"
    echo -e "${RED}--- Expected (grep):${NC}"
    cat "$GREPOUT"
    echo -e "${RED}--- Diff:${NC}"
    cat diff.txt
    failed=$((failed + 1))
    failed_tests+=("$key")
  fi
  total=$((total + 1))
done

# Clean up
rm -f "$MYOUT" "$GREPOUT" diff.txt

# Print summary
echo -e "\n======================================="
echo -e "Tests completed: $total"
echo -e "${GREEN}Tests passed: $passed${NC}"
for key in "${passed_tests[@]}"; do
  echo -e "  $key (${patterns[$key]})"
done
echo -e "${RED}Tests failed: $failed${NC}"
for key in "${failed_tests[@]}"; do
  echo -e "  $key (${patterns[$key]})"
done
echo "======================================="